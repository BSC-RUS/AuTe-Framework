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

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Pavel Golovkin
 */
public class JSScriptEngineTest {

	private JSScriptEngine jsScriptEngine = new JSScriptEngine();

	@Test
	public void noResultJSProcedureWithoutParamsTest() {
		ScriptEngineProcedureResult result = jsScriptEngine.executeProcedure("alert('Hello world');", null);
		Assert.assertFalse(result.isOk());
	}

	@Test
	public void noResultJSProcedureWithParamsTest() {
		Map<String, Object> params = new HashMap<>();
		params.put("test", "test");
		ScriptEngineProcedureResult eval = jsScriptEngine.executeProcedure("alert('Hello world');", params);
		Assert.assertFalse(eval.isOk());
	}

	@Test
	public void errorInJSProcedureTest() {
		Map<String, Object> params = new HashMap<>();
		params.put("test", "test");
		ScriptEngineProcedureResult eval = jsScriptEngine.executeProcedure("test test test", params);
		Assert.assertFalse(eval.isOk());
	}

	@Test
	public void okResultJSProcedureWithoutParamsTest() {
		Map<String, Object> params = new HashMap<>();
		params.put("test", "test");
		ScriptEngineProcedureResult eval = jsScriptEngine.executeProcedure("var a = 1;", params);
		Assert.assertTrue(eval.isOk());
	}

	@Test
	public void errorResultJSProcedureWithoutParamsTest() {
		ScriptEngineProcedureResult eval = jsScriptEngine.executeProcedure("var a = 1; stepStatus.exception = 'error';", null);
		Assert.assertFalse(eval.isOk());
	}

	@Test
	public void errorResultJSProcedureWtihParamsTest() {
		Map<String, Object> params = new HashMap<>();
		params.put("test", "test");
		ScriptEngineProcedureResult eval = jsScriptEngine.executeProcedure("var a = 1; stepStatus.exception = 'error';", params);
		Assert.assertFalse(eval.isOk());
	}

	@Test
	public void noResultJSFunctionWithoutParamsTest() {
		ScriptEngineFunctionResult result = jsScriptEngine.executeFunction("alert('Hello world');", null);
		Assert.assertFalse(result.isOk());
		Assert.assertNull(result.getResult());
	}

	@Test
	public void noResultJSFunctionWithParamsTest() {
		Map<String, Object> params = new HashMap<>();
		params.put("test", "test");
		ScriptEngineFunctionResult eval = jsScriptEngine.executeFunction("alert('Hello world');", params);
		Assert.assertFalse(eval.isOk());
		Assert.assertNull(eval.getResult());
	}

	@Test
	public void errorInJSFunctionTest() {
		Map<String, Object> params = new HashMap<>();
		params.put("test", "test");
		ScriptEngineFunctionResult eval = jsScriptEngine.executeFunction("test test test", params);
		Assert.assertFalse(eval.isOk());
		Assert.assertNull(eval.getResult());
	}

	@Test
	public void okEmptyResultJSFunctionWithoutParamsTest() {
		Map<String, Object> params = new HashMap<>();
		params.put("test", "test");
		ScriptEngineFunctionResult eval = jsScriptEngine.executeFunction("var a = 1;", params);
		Assert.assertTrue(eval.isOk());
		Assert.assertNull(eval.getResult());
	}

	@Test
	public void errorResultJSFunctionWithoutParamsTest() {
		ScriptEngineFunctionResult eval = jsScriptEngine.executeFunction("var a = 1;", null);
		Assert.assertTrue(eval.isOk());
		Assert.assertNull(eval.getResult());
	}

	@Test
	public void okResultJSFunctionWithReturnWithoutParamsTest() {
		ScriptEngineFunctionResult eval = jsScriptEngine.executeFunction("var a = '1'; a;", null);
		Assert.assertTrue(eval.isOk());
		Assert.assertEquals("1", eval.getResult());
	}

	@Test
	public void okResultJSFunctionWithReturnWithParamsTest() {
		Map<String, Object> params = new HashMap<>();
		params.put("test", "test");
		ScriptEngineFunctionResult eval = jsScriptEngine.executeFunction("var a = '1'; a;", params);
		Assert.assertTrue(eval.isOk());
		Assert.assertEquals("1", eval.getResult());
	}
}