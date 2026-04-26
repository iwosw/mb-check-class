package com.talhanation.bannermod.ai.pathfinding.async;

import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class GridAStarPathSolver implements AsyncPathSolver {
    private static final int[] DX = new int[]{1, -1, 0, 0, 0, 0};
    private static final int[] DY = new int[]{0, 0, 1, -1, 0, 0};
    private static final int[] DZ = new int[]{0, 0, 0, 0, 1, -1};

    @Override
    public PathResult solve(PathRequestSnapshot request, RegionSnapshot region, CancellationToken cancellationToken) {
        long startedAt = System.nanoTime();
        if (cancellationToken == null) {
            cancellationToken = CancellationToken.NONE;
        }
        if (region.invalidRegion()) {
            return finish(request, PathResultStatus.INVALID_SNAPSHOT, List.of(), false, 0.0D, 0, startedAt,
                    "Region snapshot is marked invalid");
        }

        LocalPoint start = toLocal(region, request.start());
        if (start == null || !isPassable(request, region, start.x, start.y, start.z)) {
            return finish(request, PathResultStatus.INVALID_SNAPSHOT, List.of(), false, 0.0D, 0, startedAt,
                    "Start position is outside snapshot or blocked");
        }

        List<LocalPoint> targetLocals = new ArrayList<>();
        for (BlockPos target : request.targets()) {
            LocalPoint localTarget = toLocal(region, target);
            if (localTarget != null) {
                targetLocals.add(localTarget);
            }
        }
        if (targetLocals.isEmpty()) {
            return finish(request, PathResultStatus.INVALID_SNAPSHOT, List.of(), false, 0.0D, 0, startedAt,
                    "No targets are inside snapshot bounds");
        }

        int volume = region.sizeX() * region.sizeY() * region.sizeZ();
        double[] gScore = new double[volume];
        int[] cameFrom = new int[volume];
        boolean[] closed = new boolean[volume];
        Arrays.fill(gScore, Double.POSITIVE_INFINITY);
        Arrays.fill(cameFrom, -1);

        PriorityQueue<NodeState> open = new PriorityQueue<>(Comparator.comparingDouble(NodeState::fScore));
        int startIndex = region.indexOf(start.x, start.y, start.z);
        gScore[startIndex] = 0.0D;
        open.add(new NodeState(startIndex, heuristic(start.x, start.y, start.z, targetLocals)));

        int bestIndex = startIndex;
        double bestHeuristic = heuristic(start.x, start.y, start.z, targetLocals);
        int visitedNodes = 0;
        int maxVisited = request.maxVisitedNodes() <= 0 ? Integer.MAX_VALUE : request.maxVisitedNodes();

        while (!open.isEmpty()) {
            if (cancellationToken.isCancelled()) {
                return buildPartialOrCancelled(request, region, cameFrom, bestIndex, gScore, visitedNodes, startedAt,
                        PathResultStatus.CANCELLED, "Cancelled");
            }
            if (deadlineExceeded(request)) {
                return buildPartialOrCancelled(request, region, cameFrom, bestIndex, gScore, visitedNodes, startedAt,
                        PathResultStatus.DEADLINE_EXCEEDED, "Deadline exceeded");
            }

            NodeState current = open.poll();
            int currentIndex = current.index();
            if (closed[currentIndex]) {
                continue;
            }
            closed[currentIndex] = true;
            visitedNodes++;

            if (visitedNodes > maxVisited) {
                return buildPartialOrCancelled(request, region, cameFrom, bestIndex, gScore, visitedNodes, startedAt,
                        PathResultStatus.PARTIAL, "Visited-node limit reached");
            }

            LocalPoint currentPoint = fromIndex(region, currentIndex);
            double h = heuristic(currentPoint.x, currentPoint.y, currentPoint.z, targetLocals);
            if (h < bestHeuristic) {
                bestHeuristic = h;
                bestIndex = currentIndex;
            }

            if (isTarget(currentPoint, targetLocals)) {
                List<BlockPos> nodes = reconstruct(region, cameFrom, currentIndex);
                return finish(request, PathResultStatus.SUCCESS, nodes, true, gScore[currentIndex], visitedNodes, startedAt, "");
            }

            for (int i = 0; i < DX.length; i++) {
                int nx = currentPoint.x + DX[i];
                int ny = currentPoint.y + DY[i];
                int nz = currentPoint.z + DZ[i];
                if (!inBounds(region, nx, ny, nz) || !isPassable(request, region, nx, ny, nz)) {
                    continue;
                }
                int neighborIndex = region.indexOf(nx, ny, nz);
                if (closed[neighborIndex]) {
                    continue;
                }

                double stepCost = region.movementCostAt(nx, ny, nz) + dynamicObstacleCost(region, nx, ny, nz);
                double tentativeG = gScore[currentIndex] + stepCost;
                if (tentativeG < gScore[neighborIndex]) {
                    cameFrom[neighborIndex] = currentIndex;
                    gScore[neighborIndex] = tentativeG;
                    double f = tentativeG + heuristic(nx, ny, nz, targetLocals);
                    open.add(new NodeState(neighborIndex, f));
                }
            }
        }

        if (bestIndex != startIndex) {
            List<BlockPos> partialNodes = reconstruct(region, cameFrom, bestIndex);
            return finish(request, PathResultStatus.PARTIAL, partialNodes, false, gScore[bestIndex], visitedNodes, startedAt,
                    "No complete path; returning best partial");
        }
        return finish(request, PathResultStatus.NO_PATH, List.of(), false, 0.0D, visitedNodes, startedAt, "No path found");
    }

    private static PathResult buildPartialOrCancelled(PathRequestSnapshot request,
                                                      RegionSnapshot region,
                                                      int[] cameFrom,
                                                      int bestIndex,
                                                      double[] gScore,
                                                      int visitedNodes,
                                                      long startedAt,
                                                      PathResultStatus terminalStatus,
                                                      String reason) {
        if (bestIndex >= 0) {
            List<BlockPos> nodes = reconstruct(region, cameFrom, bestIndex);
            if (nodes.size() > 1) {
                return finish(request, PathResultStatus.PARTIAL, nodes, false, gScore[bestIndex], visitedNodes, startedAt, reason);
            }
        }
        return finish(request, terminalStatus, List.of(), false, 0.0D, visitedNodes, startedAt, reason);
    }

    private static PathResult finish(PathRequestSnapshot request,
                                     PathResultStatus status,
                                     List<BlockPos> nodes,
                                     boolean reached,
                                     double cost,
                                     int visitedNodes,
                                     long startedAt,
                                     String reason) {
        long solveNanos = Math.max(0L, System.nanoTime() - startedAt);
        return new PathResult(
                request.entityUuid(),
                request.requestId(),
                request.epoch(),
                status,
                nodes,
                reached,
                cost,
                visitedNodes,
                solveNanos,
                reason
        );
    }

    private static boolean deadlineExceeded(PathRequestSnapshot request) {
        return request.deadlineNanos() > 0L && System.nanoTime() > request.deadlineNanos();
    }

    private static boolean isTarget(LocalPoint point, List<LocalPoint> targets) {
        for (LocalPoint target : targets) {
            if (target.x == point.x && target.y == point.y && target.z == point.z) {
                return true;
            }
        }
        return false;
    }

    private static double heuristic(int x, int y, int z, List<LocalPoint> targets) {
        double best = Double.POSITIVE_INFINITY;
        for (LocalPoint target : targets) {
            double dist = Math.abs(target.x - x) + Math.abs(target.y - y) + Math.abs(target.z - z);
            if (dist < best) {
                best = dist;
            }
        }
        return best;
    }

    private static boolean inBounds(RegionSnapshot region, int x, int y, int z) {
        return x >= 0 && x < region.sizeX() && y >= 0 && y < region.sizeY() && z >= 0 && z < region.sizeZ();
    }

    private static boolean isPassable(PathRequestSnapshot request, RegionSnapshot region, int x, int y, int z) {
        byte flags = region.flagsAt(x, y, z);
        if ((flags & RegionSnapshot.FLAG_SOLID) != 0) {
            return false;
        }
        if ((flags & RegionSnapshot.FLAG_LAVA) != 0) {
            return false;
        }
        if (request.avoidWater() && (flags & RegionSnapshot.FLAG_WATER) != 0) {
            return false;
        }
        for (DynamicObstacleSnapshot obstacle : region.dynamicObstacles()) {
            if (!obstacle.blocking()) {
                continue;
            }
            double wx = region.originMin().getX() + x + 0.5D;
            double wy = region.originMin().getY() + y + 0.5D;
            double wz = region.originMin().getZ() + z + 0.5D;
            if (wx >= obstacle.minX() && wx <= obstacle.maxX()
                    && wy >= obstacle.minY() && wy <= obstacle.maxY()
                    && wz >= obstacle.minZ() && wz <= obstacle.maxZ()) {
                return false;
            }
        }
        return true;
    }

    private static int dynamicObstacleCost(RegionSnapshot region, int x, int y, int z) {
        int extra = 0;
        double wx = region.originMin().getX() + x + 0.5D;
        double wy = region.originMin().getY() + y + 0.5D;
        double wz = region.originMin().getZ() + z + 0.5D;
        for (DynamicObstacleSnapshot obstacle : region.dynamicObstacles()) {
            if (obstacle.blocking()) {
                continue;
            }
            if (wx >= obstacle.minX() && wx <= obstacle.maxX()
                    && wy >= obstacle.minY() && wy <= obstacle.maxY()
                    && wz >= obstacle.minZ() && wz <= obstacle.maxZ()) {
                extra += Math.max(0, obstacle.additionalCost());
            }
        }
        return extra;
    }

    private static List<BlockPos> reconstruct(RegionSnapshot region, int[] cameFrom, int endIndex) {
        ArrayList<BlockPos> nodes = new ArrayList<>();
        int cursor = endIndex;
        while (cursor >= 0) {
            LocalPoint p = fromIndex(region, cursor);
            nodes.add(new BlockPos(
                    region.originMin().getX() + p.x,
                    region.originMin().getY() + p.y,
                    region.originMin().getZ() + p.z
            ));
            cursor = cameFrom[cursor];
        }
        java.util.Collections.reverse(nodes);
        return nodes;
    }

    private static LocalPoint toLocal(RegionSnapshot region, BlockPos worldPos) {
        int lx = worldPos.getX() - region.originMin().getX();
        int ly = worldPos.getY() - region.originMin().getY();
        int lz = worldPos.getZ() - region.originMin().getZ();
        if (!inBounds(region, lx, ly, lz)) {
            return null;
        }
        return new LocalPoint(lx, ly, lz);
    }

    private static LocalPoint fromIndex(RegionSnapshot region, int index) {
        int sizeX = region.sizeX();
        int sizeZ = region.sizeZ();
        int y = index / (sizeX * sizeZ);
        int remainder = index - y * sizeX * sizeZ;
        int z = remainder / sizeX;
        int x = remainder % sizeX;
        return new LocalPoint(x, y, z);
    }

    private record LocalPoint(int x, int y, int z) {
    }

    private record NodeState(int index, double fScore) {
    }
}
