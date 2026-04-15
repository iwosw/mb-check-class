# Deferred Items

- Pre-existing compile blocker outside plan 21-03 scope: worker logistics sources reference missing `com.talhanation.bannermod.logistics.BannerModLogisticsService`, `BannerModLogisticsRoute`, and `BannerModCourierTask` classes during `./gradlew compileJava`. Wave-3 bannerlord ownership changes were fixed to compile cleanly against the current tree before this unrelated blocker stopped the full root compile.
