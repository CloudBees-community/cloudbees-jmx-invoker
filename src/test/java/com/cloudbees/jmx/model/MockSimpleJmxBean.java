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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:cleclerc@cloudbees.com">Cyrille Le Clerc</a>
 */
public class MockSimpleJmxBean implements MockSimpleJmxBeanMBean {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    public String anOperationResult;
    public String zeAttribute;
    public boolean boolAttribute;
    public Boolean booleanAttribute;
    public int intAttribute;
    public Integer integerAttribute;

    @Override
    public String anOperation(String arg1, boolean arg2, Boolean arg3, Integer arg4, int arg5) {
        logger.info("anOperation({},{},{},{},{})", arg1, arg2, arg3, arg4, arg5);
        return anOperationResult;
    }

    @Override
    public String getZeAttribute() {
        return zeAttribute;
    }

    @Override
    public void setZeAttribute(String zeAttribute) {
        this.zeAttribute = zeAttribute;
    }

    @Override
    public String getAnOperationResult() {
        return anOperationResult;
    }

    @Override
    public void setAnOperationResult(String anOperationResult) {
        this.anOperationResult = anOperationResult;
    }

    @Override
    public boolean isBoolAttribute() {
        return boolAttribute;
    }

    @Override
    public void setBoolAttribute(boolean boolAttribute) {
        this.boolAttribute = boolAttribute;
    }

    @Override
    public Boolean getBooleanAttribute() {
        return booleanAttribute;
    }

    @Override
    public void setBooleanAttribute(Boolean booleanAttribute) {
        this.booleanAttribute = booleanAttribute;
    }

    @Override
    public int getIntAttribute() {
        return intAttribute;
    }

    @Override
    public void setIntAttribute(int intAttribute) {
        this.intAttribute = intAttribute;
    }

    @Override
    public Integer getIntegerAttribute() {
        return integerAttribute;
    }

    @Override
    public void setIntegerAttribute(Integer integerAttribute) {
        this.integerAttribute = integerAttribute;
    }
}
