/*
 * Copyright (c) Bosch Software Innovations GmbH 2018.
 * Copyright (c) Bosch.IO GmbH 2020.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.sw360.client.adapter;

import org.eclipse.sw360.antenna.sw360.client.rest.SW360ComponentClient;
import org.eclipse.sw360.antenna.sw360.client.utils.SW360ClientException;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.SW360HalResourceUtility;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.components.SW360Component;
import org.eclipse.sw360.antenna.sw360.client.rest.resource.components.SW360SparseComponent;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.eclipse.sw360.antenna.sw360.client.utils.FutureUtils.failedFuture;
import static org.eclipse.sw360.antenna.sw360.client.utils.FutureUtils.optionalFuture;

class SW360ComponentClientAdapterAsyncImpl implements SW360ComponentClientAdapterAsync {
    private final SW360ComponentClient componentClient;

    public SW360ComponentClientAdapterAsyncImpl(SW360ComponentClient client) {
        componentClient = client;
    }

    @Override
    public SW360ComponentClient getComponentClient() {
        return componentClient;
    }

    @Override
    public CompletableFuture<Optional<SW360Component>> getOrCreateComponent(SW360Component componentFromRelease) {
        if (componentFromRelease.getComponentId() != null) {
            return getComponentById(componentFromRelease.getComponentId());
        }
        return getComponentByName(componentFromRelease.getName())
                .thenCompose(optComponent -> optComponent.isPresent() ?
                        CompletableFuture.completedFuture(optComponent) :
                        createComponent(componentFromRelease)
                                .thenApply(Optional::of));
    }

    @Override
    public CompletableFuture<SW360Component> createComponent(SW360Component component) {
        if (!SW360ComponentAdapterUtils.isValidComponent(component)) {
            return failedFuture(new SW360ClientException("Can not write invalid component for " + component.getName()));
        }
        return getComponentClient().createComponent(component);
    }

    @Override
    public CompletableFuture<Optional<SW360Component>> getComponentById(String componentId) {
        return optionalFuture(getComponentClient().getComponent(componentId));
    }

    @Override
    public CompletableFuture<Optional<SW360Component>> getComponentByName(String componentName) {
        return getComponentClient().searchByName(componentName)
                .thenCompose(components ->
                        components.stream()
                                .filter(c -> c.getName().equals(componentName))
                                .map(c -> SW360HalResourceUtility.getLastIndexOfSelfLink(c.getLinks()).orElse(""))
                                .map(this::getComponentById)
                                .findFirst()
                                .orElse(CompletableFuture.completedFuture(Optional.empty()))
                );
    }

    @Override
    public CompletableFuture<List<SW360SparseComponent>> getComponents() {
        return getComponentClient().getComponents();
    }
}
