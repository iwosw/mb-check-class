#!/usr/bin/env bash
# Source this file to put compact context wrappers before raw shell tools.

AI_CONTEXT_PROXY_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
export PATH="$AI_CONTEXT_PROXY_ROOT/wrappers:$AI_CONTEXT_PROXY_ROOT/bin:$PATH"
