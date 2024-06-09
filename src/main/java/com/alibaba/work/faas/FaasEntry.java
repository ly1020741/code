package com.alibaba.work.faas;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import com.alibaba.work.faas.common.AbstractEntry;
import com.alibaba.work.faas.common.FaasInputs;
import com.alibaba.work.faas.util.DingOpenApiUtil;
import com.alibaba.work.faas.util.YidaConnectorUtil;
import com.alibaba.work.faas.util.YidaConnectorUtil.ConnectorTypeEnum;
import com.aliyun.dingtalkyida_1_0.models.BatchSaveFormDataRequest;

import org.apache.commons.lang3.StringUtils;
import com.alibaba.work.faas.util.DESUtil;

/**
 * 您的业务逻辑请从此类开始
 * 
 * @Date 2023/09/21 11:16 AM
 * @Description 宜搭Faas连接器函数入口，FC handler：com.alibaba.work.faas.FaasEntry::handleRequest
 * @Version 2.0
 * @Author bufan
 * 
 **/
public class FaasEntry extends AbstractEntry {
    @Override
    public JSONObject execute(FaasInputs faasInputs) {
		System.out.println("faasInputs: " + JSON.toJSONString(faasInputs));
		//填充宜搭工具类的上下文, 必须调用此方法!!! 请不要删除
		initYidaUtil(faasInputs);

		//登录态里的钉钉userId
		String userId = (String) faasInputs.getYidaContext().get("userId");
		//登录态里的钉钉corpId
		String corpId = (String) faasInputs.getYidaContext().get("corpId");

		//业务传的实际入参(如果您配置了参数映射(也就是点击了连接器工厂里的解析Body按钮并配置了各个参数描述和映射), 那么就是业务实际参数经过参数映射处理后的值)
		Map<String,Object> input = faasInputs.getInputs();

		/**
		 * 示例1, 在doYourBusiness方法里编写您的业务代码, 也可以将业务代码封装到其他Class源文件或方法里, cloudIDE和您的本地IDE基本无区别。
		 */
		 JSONObject result = new JSONObject();
		 try {
		    Object businessResult = doYourBusiness(input);
		    result.put("success",true);
		    result.put("result",businessResult);
		    result.put("error","");
		    return result;
		 } catch (Exception e) {
		    result.put("success",false);
		    result.put("result",null);
		    result.put("error",e.getMessage());
		    return result;
		 }
	}

	/**
	 * 将相关参数存到宜搭工具类里, 必须要调用此方法!!! 请不要删除!!!
	 *
	 * @param faasInputs
	 */
	private static final void initYidaUtil(FaasInputs faasInputs){
		/**
		 * 用于调用钉钉开放平台OpenAPI的accessToken, 宜搭提供的, 仅申请了钉钉开放平台的部分OpenAPI的调用权限
		 * 如果此accessToken不满足您的需求,可在钉钉开放平台创建您自己的钉钉应用并获取appKey和APPSecret并使用com.alibaba.work.faas.util.DingOpenApiUtil获取您自己的customAccessToken
		 *
		 * @see com.alibaba.work.faas.util.DingOpenApiUtil#getCustomAccessTokenThenCache(String,String)
		 */
		String accessToken = (String) faasInputs.getYidaContext().get("accessToken");
		// 设置钉开放平台访问token, 后续无需再设置
		DingOpenApiUtil.setAccessToken(accessToken);

		// try {
		//     //调用该方法就会自动存储customAccessToken, 之后请不要调用DingOpenApiUtil.setAccessToken(accessToken)将返回的customAccessToken覆盖宜搭传入的accessToken;
		//     DingOpenApiUtil.getCustomAccessTokenThenCache("您的钉钉应用appKey", "您的钉钉应用appSecret");
		// } catch (Exception e) {
		//     System.out.println("getCustomAccessTokenThenCache error:"+e.getMessage());
		// }
		/**
		 *调用宜搭连接器依赖consumeCode
		 */
		String consumeCode = (String)faasInputs.getYidaContext().get("consumeCode");
		//设置连接器消费码, 后续无需再设置
		YidaConnectorUtil.setConsumeCode(consumeCode);
	}

	/**
	 * 可以将您的业务代码放在这里
	 *
	 * @param input
	 * @return
	 * @throws Exception
	 */
	private Object doYourBusiness(Map<String,Object> input) throws Exception{
		String content = (String)input.get("content");
String password = (String)input.get("password");
Integer type = Integer.parseInt(String.valueOf(input.get("type")));
/**
*在这里编写您的业务代码, 也可以将业务代码封装到其他类或方法里.
*/
JSONObject result = new JSONObject();
result.put("success",false);
result.put("result","");
result.put("error","");
if (0 == type) {
/**
* 加密
*/
String encryptContent = DESUtil.encrypt(content, password);
System.out.println("加密后的字符串:" + encryptContent);
if (StringUtils.isEmpty(encryptContent)) {
result.put("error", "empty string got!");
return result;
}
result.put("result", encryptContent);
result.put("success", true);
}
else {
/**
* 解密
*/
String encryptContent = DESUtil.decrypt(content, password);
System.out.println("解密后的字符串++++++:" + encryptContent);
if (StringUtils.isEmpty(encryptContent)) {
result.put("error", "empty string got!");
return result;
}
result.put("result", encryptContent);
result.put("success", true);
}
System.out.println("返回:" + JSON.toJSONString(result));
return result.get("result");
	}

	/**
	 * 示例 调用钉钉官方连接器(待办连接器, 创建待办)
	 *
	 * @param faasInputs
	 * @return
	 * @throws Exception
	 */
	private Object  invokeYidaConnector(FaasInputs faasInputs) throws Exception {
		Map<String,Object> input = faasInputs.getInputs();
		String userId = (String)faasInputs.getYidaContext().get("userId");
		String corpId = (String)faasInputs.getYidaContext().get("corpId");
		JSONObject connectorActionInputs = new JSONObject();
		//注意: http连接器的入参是body,query等, 钉钉官方连接器则不是, 请参考宜搭帮助文档 https://www.yuque.com/yida/support/stbfik
		{
			connectorActionInputs.put("unionId", Arrays.asList(userId));
			connectorActionInputs.put("subject",StringUtils.isNotBlank((String)input.get("title"))?(String)input.get("title"):"Java版Faas连接器创建的待办");
			connectorActionInputs.put("creatorId",Arrays.asList(userId));
			connectorActionInputs.put("description","Faas连接器里调用钉钉官方连接器创建待办");
			connectorActionInputs.put("dueTime",System.currentTimeMillis()+600*1000);
			connectorActionInputs.put("priority",10L);
		}
		//请确保调用前设置了consumeCode
		//更多钉钉官方连接器ID和动作ID及出入参请参照  https://www.yuque.com/yida/support/stbfik#Mv0dK (如果目标段落不存在, 请访问 https://www.yuque.com/yida/support/stbfik)
		YidaConnectorUtil.YidaResponse response = YidaConnectorUtil.invokeService("G-CONN-1016B8AEBED50B01B8D00009","G-ACT-1016B8B1911A0B01B8D0000I", ConnectorTypeEnum.DING_INNER_CONNECTOR,null,connectorActionInputs);
		if (Objects.nonNull(response) && response.isSuccess()){
			try {
				return YidaConnectorUtil.extractYidaConnectorExecutionResult(response);
			} catch (Exception e) {
				throw new Exception(e.getMessage());
			}
		}else {
			throw new Exception(Objects.isNull(response)?"执行宜搭连接器失败":response.getErrorCode() +" "+response.getErrorMsg());
		}
	}

	/**
	 * 示例 批量创建宜搭表单实例
	 *
	 * @return
	 * @throws Exception
	 */
	private List<String> invokeDingOpenApi() throws Exception {
		BatchSaveFormDataRequest request = new BatchSaveFormDataRequest();
		request.setAsynchronousExecution(true);
		request.setKeepRunningAfterException(true);
		request.setNoExecuteExpression(true);
		request.setAppType("宜搭应用编码");
		request.setUserId("钉钉userId");
		request.setSystemToken("宜搭应用systemToken");
		request.setFormUuid("表单编码");

		JSONObject formData = new JSONObject();
		formData.put("表单组件1的ID", "表单组件1的值");
		formData.put("表单组件2的ID", "表单组件2的值");
		//批量创建200条
		request.setFormDataJsonList(Collections.nCopies(200,formData.toJSONString()));

		return DingOpenApiUtil.batchSaveFormData(request);
    }
}