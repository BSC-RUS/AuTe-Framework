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

import lombok.extern.slf4j.Slf4j;

import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Map;

/**
 * @author Pavel Golovkin
 */
@Slf4j
public class JSScriptEngine implements ScriptEngine {

	private static final String STEP_STATUS = "stepStatus";
	private static final String RESPONSE = "response";
	private static final String SCENARIO_VARIABLES = "scenarioVariables";

	private static final ScriptEngineExecutionResult ERROR_RESULT = new ScriptEngineExecutionResult();
	static {
		ERROR_RESULT.setException("error executing script");
	}

	private static final ScriptEngineExecutionResult EMPTY_RESULT = new ScriptEngineExecutionResult();

	private javax.script.ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("js");

	public JSScriptEngine() {
		scriptEngine.put(STEP_STATUS, new ScriptEngineExecutionResult());
		scriptEngine.put(RESPONSE, null);
	}

	@Override
	public ScriptEngineProcedureResult executeProcedure(String script, Map<String, ? super Object> variables) {
		log.debug("Execute script procedure {} with variables {}", script, variables);
		scriptEngine.put(SCENARIO_VARIABLES, variables);
		ScriptEngineExecutionResult scriptEngineExecutionResult;
		try {
			scriptEngine.eval(script);
		} catch (ScriptException ex) {
			log.error("Error evaluating script procedure {}", script, ex);
			return ERROR_RESULT;
		}
		scriptEngineExecutionResult = (ScriptEngineExecutionResult) scriptEngine.get(STEP_STATUS);
		if (!scriptEngineExecutionResult.isOk()) {
			log.error("Error executing script procedure", scriptEngineExecutionResult.getException());
		}

		return scriptEngineExecutionResult;
	}

	@Override
	public ScriptEngineFunctionResult executeFunction(String script, Map<String, ? super Object> variables) {
		log.debug("Execute script function {} with variables {}", script, variables);
		ScriptEngineExecutionResult scriptEngineExecutionResult;
		scriptEngine.put(SCENARIO_VARIABLES, variables);
		try {
			Object result = scriptEngine.eval(script);
			if (result != null) {
				log.debug("Script execution result {}", result);
				scriptEngineExecutionResult = new ScriptEngineExecutionResult();
				scriptEngineExecutionResult.setResult(String.valueOf(result));
				return scriptEngineExecutionResult;
			}
			return EMPTY_RESULT;
		} catch (ScriptException ex) {
			log.error("Error evaluating script function {}", script, ex);
			return ERROR_RESULT;
		}
	}
}
