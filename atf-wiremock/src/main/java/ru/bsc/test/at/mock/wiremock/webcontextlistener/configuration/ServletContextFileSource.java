/*
 * Copyright 2018 BSC Msc, LLC
 *
 * This file is part of the AuTe Framework project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.bsc.test.at.mock.wiremock.webcontextlistener.configuration;

import com.github.tomakehurst.wiremock.common.AbstractFileSource;
import com.github.tomakehurst.wiremock.common.FileSource;

import java.io.File;

/**
 * Created by sdoroshin on 04.08.2017.
 *
 */
class ServletContextFileSource extends AbstractFileSource {

    private final String rootPath;

    ServletContextFileSource(String rootPath) {
        super(new File(rootPath));
        this.rootPath = rootPath;
    }

    @Override
    public FileSource child(String subDirectoryName) {
        return new ServletContextFileSource(rootPath + '/' + subDirectoryName);
    }

    @Override
    protected boolean readOnly() {
        return true;
    }
}

