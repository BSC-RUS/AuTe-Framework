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

package ru.bsc.test.at.executor.step.executor.scriptengine;

import java.util.Map;

/**
 * Interface for performing inner script written on programming language.
 *
 * @author Pavel Golovkin
 */
public interface ScriptEngine {

	/**
	 * Executes procedure with no return value. You can define is it Ok or Not using {@link ScriptEngineProcedureResult#isOk()}.
	 * @param script script to execute
	 * @param variables script's variables
	 * @return procedure execution result
	 */
	ScriptEngineProcedureResult executeProcedure(String script, Map<String, ? super Object> variables);

	/**
	 * Executes function with return value. And you can define was execution Ok or Not {@link ScriptEngineFunctionResult#isOk()}
	 * @param script script to execute
	 * @param variables script's variables
	 * @return script execution result in {@link ScriptEngineFunctionResult#getResult()}
	 */
	ScriptEngineFunctionResult executeFunction(String script, Map<String, ? super Object> variables);
}
