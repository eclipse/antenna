/*
 * Copyright (c) Bosch Software Innovations GmbH 2016-2017.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.antenna.util;

import org.eclipse.sw360.antenna.model.artifact.Artifact;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactFilename;
import org.eclipse.sw360.antenna.model.artifact.facts.ArtifactIssues;
import org.eclipse.sw360.antenna.model.artifact.facts.DeclaredLicenseInformation;
import org.eclipse.sw360.antenna.model.xml.generated.Issue;
import org.eclipse.sw360.antenna.model.xml.generated.LicenseInformation;
import org.eclipse.sw360.antenna.model.xml.generated.SecurityIssueStatus;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonReaderTest {
    private List<Artifact> artifacts;

    @Test
    public void testMapLicenses() throws URISyntaxException, IOException{
        Path recordFilePath = Paths.get(".", "target", "foo");
        JsonReader jsonReader = new JsonReader(recordFilePath, Paths.get("tmp"), Charset.forName("UTF-8"));
        URI uri = this.getClass().getClassLoader().getResource("data.json").toURI();
        InputStream iStream = Files.newInputStream(Paths.get(uri));
        artifacts = jsonReader.createArtifactsList(iStream);
        assertThat(artifacts.get(0).askForGet(DeclaredLicenseInformation.class).get().evaluate()).isEqualTo("( license1 AND ( license2 AND license3 ) )");
    }

    @Test
    public void testParseData2() throws URISyntaxException, IOException{
        Path recordFilePath = Paths.get(".", "target", "foo");
        JsonReader jsonReader = new JsonReader(recordFilePath, Paths.get("tmp"), Charset.forName("UTF-8"));
        URI uri = this.getClass().getClassLoader().getResource("data2.json").toURI();
        InputStream iStream = Files.newInputStream(Paths.get(uri));

        artifacts = jsonReader.createArtifactsList(iStream);

        assertThat(artifacts.stream()
                .map(artifact -> artifact.askForGet(DeclaredLicenseInformation.class))
                .map(Optional::get)
                .map(LicenseInformation::evaluate)
                .anyMatch("PUBLIC-DOMAIN"::equals));
        assertThat(artifacts.stream()
                .map(Artifact::getAnalysisSource)
                .allMatch("CSV"::equals));
        assertThat(artifacts.stream()
                .map(artifact -> artifact.askFor(ArtifactFilename.class))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(ArtifactFilename::getFilename)
                .anyMatch("aopalliance-1.0.jar"::equals));

        artifacts.stream()
                .map(artifact -> artifact.askForGet(ArtifactIssues.class))
                .map(Optional::get)
                .findAny()
                .ifPresent(issues -> assertThat(issues.stream()
                        .map(Issue::getStatus)
                        .anyMatch(SecurityIssueStatus.OPEN::equals)));

        artifacts.stream()
                .map(artifact -> artifact.askForGet(ArtifactIssues.class))
                .map(Optional::get)
                .findAny()
                .ifPresent(issues -> assertThat(issues.stream()
                        .map(Issue::getReference)
                        .anyMatch("CVE-2009-1523"::equals)));
    }
}
