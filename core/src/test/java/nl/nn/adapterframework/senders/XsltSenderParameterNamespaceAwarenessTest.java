package nl.nn.adapterframework.senders;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import nl.nn.adapterframework.parameters.Parameter;
import nl.nn.adapterframework.parameters.Parameter.ParameterType;
import nl.nn.adapterframework.stream.Message;
import nl.nn.adapterframework.testutil.TestFileUtils;

public class XsltSenderParameterNamespaceAwarenessTest extends SenderTestBase<XsltSender> {

	private String EXPECTED_RESULT_PARAMETER_PLAIN_SENDER_NAMESPACE_UNAWARE_7_5= "/Xslt/3205/result-ok-with-namespaces.xml";
	private String EXPECTED_RESULT_PARAMETER_PLAIN_SENDER_NAMESPACE_UNAWARE_7_6= "/Xslt/3205/result-param-contents-not-found-with-namespaces.xml";
	private String EXPECTED_RESULT_PARAMETER_PLAIN_SENDER_NAMESPACE_UNAWARE_7_7= "/Xslt/3205/result-param-contents-not-found-no-namespaces.xml";
	private String EXPECTED_RESULT_PARAMETER_PLAIN_SENDER_NAMESPACE_UNAWARE_7_8= "/Xslt/3205/result-param-contents-not-found-no-namespaces.xml";

	private String EXPECTED_RESULT_PARAMETER_PLAIN_SENDER_NAMESPACE_AWARE_7_5= "/Xslt/3205/result-ok-with-namespaces.xml";
	private String EXPECTED_RESULT_PARAMETER_PLAIN_SENDER_NAMESPACE_AWARE_7_6= "/Xslt/3205/result-param-contents-not-found-with-namespaces.xml";
	private String EXPECTED_RESULT_PARAMETER_PLAIN_SENDER_NAMESPACE_AWARE_7_7= "/Xslt/3205/result-param-contents-not-found-with-namespaces.xml";
	private String EXPECTED_RESULT_PARAMETER_PLAIN_SENDER_NAMESPACE_AWARE_7_8= "/Xslt/3205/result-param-contents-not-found-with-namespaces.xml";

	private String EXPECTED_RESULT_PARAMETER_REMOVE_NAMESPACES_SENDER_NAMESPACE_UNAWARE_7_5= "/Xslt/3205/result-ok-with-param-namespaces-removed.xml";
	private String EXPECTED_RESULT_PARAMETER_REMOVE_NAMESPACES_SENDER_NAMESPACE_UNAWARE_7_6= "/Xslt/3205/result-ok-with-param-namespaces-removed.xml";
	private String EXPECTED_RESULT_PARAMETER_REMOVE_NAMESPACES_SENDER_NAMESPACE_UNAWARE_7_7= "/Xslt/3205/result-ok-with-all-namespaces-removed.xml";
	private String EXPECTED_RESULT_PARAMETER_REMOVE_NAMESPACES_SENDER_NAMESPACE_UNAWARE_7_8= "/Xslt/3205/result-ok-with-all-namespaces-removed.xml";

	private String EXPECTED_RESULT_PARAMETER_REMOVE_NAMESPACES_SENDER_NAMESPACE_AWARE_7_5= "/Xslt/3205/result-ok-with-param-namespaces-removed.xml";
	private String EXPECTED_RESULT_PARAMETER_REMOVE_NAMESPACES_SENDER_NAMESPACE_AWARE_7_6= "/Xslt/3205/result-ok-with-param-namespaces-removed.xml";
	private String EXPECTED_RESULT_PARAMETER_REMOVE_NAMESPACES_SENDER_NAMESPACE_AWARE_7_7= "/Xslt/3205/result-ok-with-param-namespaces-removed.xml";
	private String EXPECTED_RESULT_PARAMETER_REMOVE_NAMESPACES_SENDER_NAMESPACE_AWARE_7_8= "/Xslt/3205/result-ok-with-param-namespaces-removed.xml";

	private String expectedResultParameterPlainSenderNamespaceUnaware = EXPECTED_RESULT_PARAMETER_PLAIN_SENDER_NAMESPACE_UNAWARE_7_8;
	private String expectedResultParameterPlainSenderNamespaceAware = EXPECTED_RESULT_PARAMETER_PLAIN_SENDER_NAMESPACE_AWARE_7_8;
	private String expectedResultParameterRemoveNamespacesSenderNamespaceUnaware = EXPECTED_RESULT_PARAMETER_REMOVE_NAMESPACES_SENDER_NAMESPACE_UNAWARE_7_8;
	private String expectedResultParameterRemoveNamespacesSenderNamespaceAware = EXPECTED_RESULT_PARAMETER_REMOVE_NAMESPACES_SENDER_NAMESPACE_AWARE_7_8;
	
	@Override
	public XsltSender createSender() {
		return new XsltSender();
	}

	public void testNamespaceAwarenessOfParameter(boolean senderNamespaceAware, boolean paramNRemoveNamespaces, String expectedFile) throws Exception {
		String input = TestFileUtils.getTestFile("/Xslt/3205/input.xml");
		String paramContents = TestFileUtils.getTestFile("/Xslt/3205/param.xml");
		String expectedResult = TestFileUtils.getTestFile(expectedFile);
		
		sender.setStyleSheetName("/Xslt/3205/xslt.xslt");
		sender.setNamespaceAware(senderNamespaceAware);
		Parameter param = new Parameter();
		param.setName("getPartiesOnAgreementRLY");
		param.setValue(paramContents);
		param.setType(ParameterType.NODE);
		param.setRemoveNamespaces(paramNRemoveNamespaces);
		sender.addParameter(param);
		sender.configure();
		sender.open();
		
		Message result = sender.sendMessage(new Message(input), session);
		
		assertEquals(expectedResult, result.asString());
	}

	@Test
	public void testNamespaceAwarenessOfParameterPlainSenderNamespaceUnaware() throws Exception {
		testNamespaceAwarenessOfParameter(false, false, expectedResultParameterPlainSenderNamespaceUnaware);
	}

	@Test
	public void testNamespaceAwarenessOfParameterPlainSenderNamespaceAware() throws Exception {
		testNamespaceAwarenessOfParameter(true, false, expectedResultParameterPlainSenderNamespaceAware);
	}
	
	@Test
	public void testNamespaceAwarenessOfParameterRemoveNamespacesSenderNamespaceUnaware() throws Exception {
		testNamespaceAwarenessOfParameter(false, true, expectedResultParameterRemoveNamespacesSenderNamespaceUnaware);
	}
	
	@Test
	public void testNamespaceAwarenessOfParameterRemoveNamespacesSenderNamespaceAware() throws Exception {
		testNamespaceAwarenessOfParameter(true, true, expectedResultParameterRemoveNamespacesSenderNamespaceAware);
	}
	

}