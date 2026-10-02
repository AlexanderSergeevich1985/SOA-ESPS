package com.soaesps.jetdag.dto;

import java.util.UUID;

public record TaskDependencyDto(
        UUID upstreamTaskId,
        UUID downstreamTaskId
) {}