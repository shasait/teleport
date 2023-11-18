package de.hasait.common.service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface Provider {

    @Nonnull
    String getId();

    @Nonnull
    String getDescription();

    @Nullable
    String getDisabledReason();

    @Nullable
    String validateConfig(@Nullable String config);

}
