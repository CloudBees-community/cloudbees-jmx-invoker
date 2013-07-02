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
package org.kohsuke.args4j.spi;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * @author <a href="mailto:cleclerc@cloudbees.com">Cyrille Le Clerc</a>
 */
public class ObjectNameOptionHandler extends OptionHandler<ObjectName> {

    public ObjectNameOptionHandler(CmdLineParser parser, OptionDef option, Setter<? super ObjectName> setter) {
        super(parser, option, setter);
    }

    @Override
    public int parseArguments(Parameters params) throws CmdLineException {
        String param = params.getParameter(0);
        try {
            setter.addValue(new ObjectName(param));
            return 1;
        } catch (MalformedObjectNameException e) {
            throw new CmdLineException(owner, Messages.ILLEGAL_OPERAND.format(params.getParameter(-1), param));
        }
    }

    @Override
    public String getDefaultMetaVariable() {
        return "OBJECT_NAME";
    }
}
