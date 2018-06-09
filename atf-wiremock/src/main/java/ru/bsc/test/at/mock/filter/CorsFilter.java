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

package ru.bsc.test.at.mock.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by sdoroshin on 03.08.2017.
 *
 */
public class CorsFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) {
        //no initialization logic
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        ((HttpServletResponse)response).addHeader("Access-Control-Allow-Origin", "*");
        ((HttpServletResponse)response).addHeader("Access-Control-Allow-Headers", "*");
        ((HttpServletResponse)response).addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        ((HttpServletResponse)response).addHeader("Allow", "GET, POST, PUT, DELETE, OPTIONS");
        if ("OPTIONS".equals(((HttpServletRequest)request).getMethod())) {
            ((HttpServletResponse)response).setStatus(200);
            return;
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        //no destroy logic
    }
}
