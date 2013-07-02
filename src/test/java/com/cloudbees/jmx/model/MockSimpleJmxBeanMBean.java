/*
 * Copyright 2010-2013, CloudBees Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cloudbees.jmx.model;

/**
 * @author <a href="mailto:cleclerc@cloudbees.com">Cyrille Le Clerc</a>
 */
public interface MockSimpleJmxBeanMBean {

    String anOperation(String arg1, boolean arg2, Boolean arg3, Integer arg4, int arg5);

    String getZeAttribute();

    void setZeAttribute(String zeAttribute);

    String getAnOperationResult();

    void setAnOperationResult(String anOperationResult);

    boolean isBoolAttribute();

    void setBoolAttribute(boolean boolAttribute);

    Boolean getBooleanAttribute();

    void setBooleanAttribute(Boolean booleanAttribute);

    int getIntAttribute();

    void setIntAttribute(int intAttribute);

    Integer getIntegerAttribute();

    void setIntegerAttribute(Integer integerAttribute);
}
