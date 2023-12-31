/*
 * Copyright 2023 Apple, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.opsmx.oes.pipelinebuilder;

import com.opsmx.oes.pipelinebuilder.json.Stage;
import com.opsmx.oes.pipelinebuilder.json.notifications.EmailNotification;
import com.opsmx.oes.pipelinebuilder.json.notifications.NotificationEvent;
import com.opsmx.oes.pipelinebuilder.json.stages.model.StageTypes;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests the builder for {@link Stage}.
 */
public class StageBuilderTests {

    @Test
    public void emptyBuilderThrows() {
        Assertions.assertThrows(NullPointerException.class, () -> Stage.builder().build());
    }

    @Test
    public void nameButNoTypeThrows() {
        NullPointerException thrown = Assertions.assertThrows(NullPointerException.class, () -> Stage.builder().name("test").build());
        Assertions.assertTrue(thrown.getMessage().contains("type"));
    }

    @Test
    public void typeButNoNameThrows() {
        NullPointerException thrown = Assertions.assertThrows(NullPointerException.class, () -> Stage.builder().type("test").build());
        Assertions.assertTrue(thrown.getMessage().contains("name"));
    }

    @Test
    public void nameAndTypeAreEnough() {
        Stage.builder().name("foo").type("test").build();
    }

    @Test
    public void contextTypesAndValuesArePreserved() {
        Map<String, Object> contextMap = Map.of("boolean", true,
            "string", "hello world",
            "integer", 42,
            "float", 3.1415f,
            "map", Map.of("intval", 1729));

        Stage stage = Stage.builder()
            .type("foo")
            .name("test")
            .context(contextMap)
            .build();
        contextMap.forEach((key, value) -> Assertions.assertEquals(value, stage.get(key), "Invalid context value: " + key));
    }

    @Test
    public void contextKeysCanOverrideBuilderFields() {
        Stage stage = Stage.builder()
            .id("1")
            .name("foo")
            .type("bar")
            .context(Map.of("refId", "new id", "name", "new name", "type", "new type"))
            .build();
        Assertions.assertEquals("new id", stage.get("refId"));
        Assertions.assertEquals("new name", stage.get("name"));
        Assertions.assertEquals("new type", stage.get("type"));
        Assertions.assertEquals("new id", stage.getId());
        Assertions.assertEquals("new name", stage.getName());
        Assertions.assertEquals("new type", stage.getType());
    }

    @Test
    public void stageBuilderRejectsPipelineNotifications() throws Throwable {
        for (final NotificationEvent event : NotificationEvent.values()) {
            Executable executable = () -> Stage.builder()
                .name("pipeline notification")
                .type("wait")
                .notifications(List.of(EmailNotification.builder()
                    .message(Map.of(event, "event message"))
                    .build()))
                .build();
            if (event.isPipelineEvent()) {
                Assertions.assertThrows(IllegalArgumentException.class, executable);
            } else {
                executable.execute(); // no exception
            }
        }
    }

    @ParameterizedTest(name = "failPipeline={0}, completeOtherBranchesThenFail={1}, continuePipeline={2}")
    @MethodSource("provideBooleanValues")
    public void stageBuilderManualFailures(Boolean failPipeline, Boolean completeOtherBranchesThenFail, Boolean continuePipeline) {
        Stage stage = Stage.builder()
                .name("wait stage")
                .type(StageTypes.WAIT)
                .failPipeline(failPipeline)
                .completeOtherBranchesThenFail(completeOtherBranchesThenFail)
                .continuePipeline(continuePipeline)
                .build();
        Assertions.assertEquals(failPipeline, stage.get("failPipeline"));
        Assertions.assertEquals(completeOtherBranchesThenFail, stage.get("completeOtherBranchesThenFail"));
        Assertions.assertEquals(continuePipeline, stage.get("continuePipeline"));
    }

    /**
     * @return a stream of all possible triplets of booleans
     */
    private static Stream<Object[]> provideBooleanValues() {
        return IntStream.range(0, 8).boxed()
                .map(i -> new Object[] { (i & 4) != 0, (i & 2) != 0, (i & 1) != 0 });
    }
}
