/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.sqoop.json;

import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.sqoop.common.Direction;
import org.apache.sqoop.json.util.BeanTestUtil;
import org.apache.sqoop.model.MJob;
import org.apache.sqoop.model.MStringInput;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.testng.annotations.Test;

public class TestJobBean {

  @Test
  public void testJobSerialization() throws ParseException {
    Date created = new Date();
    Date updated = new Date();
    MJob job = BeanTestUtil.createJob("ahoj", "The big Job", 22L, created, updated);

    // Fill some data at the beginning
    MStringInput input = (MStringInput) job.getFromJobConfig().getConfigs().get(0)
        .getInputs().get(0);
    input.setValue("Hi there!");
    input = (MStringInput) job.getToJobConfig().getConfigs().get(0).getInputs().get(0);
    input.setValue("Hi there again!");

    // Serialize it to JSON object
    JobBean jobBean = new JobBean(job);
    JSONObject json = jobBean.extract(false);

    // "Move" it across network in text form
    String jobJsonString = json.toJSONString();

    // Retrieved transferred object
    JSONObject parsedJobJson = JSONUtils.parse(jobJsonString);
    JobBean parsedJobBean = new JobBean();
    parsedJobBean.restore(parsedJobJson);
    MJob target = parsedJobBean.getJobs().get(0);

    // Check id and name
    assertEquals(22L, target.getPersistenceId());
    assertEquals("The big Job", target.getName());

    assertEquals(target.getFromLinkName(), "fromLinkName");
    assertEquals(target.getToLinkName(), "toLinkName");
    assertEquals(target.getFromConnectorName(), "from_ahoj");
    assertEquals(target.getToConnectorName(), "to_ahoj");
    assertEquals(created, target.getCreationDate());
    assertEquals(updated, target.getLastUpdateDate());
    assertEquals(false, target.getEnabled());

    // Test that value was correctly moved
    MStringInput targetInput = (MStringInput) target.getFromJobConfig().getConfigs()
        .get(0).getInputs().get(0);
    assertEquals("Hi there!", targetInput.getValue());
    targetInput = (MStringInput) target.getToJobConfig().getConfigs().get(0).getInputs()
        .get(0);
    assertEquals("Hi there again!", targetInput.getValue());
  }

  @Test
  public void testJobsSerialization() throws ParseException {
    Date created = new Date();
    Date updated = new Date();
    MJob job1 = BeanTestUtil.createJob("ahoj", "The big Job", 22L, created, updated);
    MJob job2 = BeanTestUtil.createJob("ahoj", "The small Job", 44L, created, updated);

    List<MJob> jobs = new ArrayList<>();
    jobs.add(job1);
    jobs.add(job2);

    // Fill some data at the beginning
    MStringInput input = (MStringInput) job1.getFromJobConfig().getConfigs().get(0)
        .getInputs().get(0);
    input.setValue("Hi there!");
    input = (MStringInput) job1.getToJobConfig().getConfigs().get(0).getInputs().get(0);
    input.setValue("Hi there again!");

    // Serialize it to JSON object
    JobBean jobsBean = new JobBean(jobs);
    JSONObject json = jobsBean.extract(false);

    // "Move" it across network in text form
    String jobJsonString = json.toJSONString();

    // Retrieved transferred object
    JSONObject parsedJobsJson = JSONUtils.parse(jobJsonString);
    JobBean parsedJobsBean = new JobBean();
    parsedJobsBean.restore(parsedJobsJson);
    assertEquals(parsedJobsBean.getJobs().size(), 2);
    MJob retrievedJob1 = parsedJobsBean.getJobs().get(0);
    MJob retrievedJob2 = parsedJobsBean.getJobs().get(1);

    // Check id and name
    assertEquals(22L, retrievedJob1.getPersistenceId());
    assertEquals("The big Job", retrievedJob1.getName());

    assertEquals(44L, retrievedJob2.getPersistenceId());
    assertEquals("The small Job", retrievedJob2.getName());

    assertEquals(retrievedJob1.getFromLinkName(), "fromLinkName");
    assertEquals(retrievedJob1.getToLinkName(), "toLinkName");
    assertEquals(retrievedJob1.getFromConnectorName(), "from_ahoj");
    assertEquals(retrievedJob1.getToConnectorName(), "to_ahoj");
    assertEquals(created, retrievedJob1.getCreationDate());
    assertEquals(updated, retrievedJob1.getLastUpdateDate());
    assertEquals(false, retrievedJob1.getEnabled());

    // Test that value was correctly moved
    MStringInput targetInput = (MStringInput) retrievedJob1.getFromJobConfig()
        .getConfigs().get(0).getInputs().get(0);
    assertEquals("Hi there!", targetInput.getValue());
    targetInput = (MStringInput) retrievedJob1.getToJobConfig().getConfigs().get(0)
        .getInputs().get(0);
    assertEquals("Hi there again!", targetInput.getValue());
  }

}
