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

import com.opsmx.oes.pipelinebuilder.json.Pipeline;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.util.List;

public class PipelineRolesTests {

    @Test
    public void pipelineRolesCanBeBuilt() {
        String role = "role1";
        List<String> roles = List.of(role);
        Pipeline pipeline = Pipeline.builder().roles(roles).name("pipelineRolesTest").build();
        Assertions.assertEquals(List.of(role), pipeline.getRoles());
    }
}
