package gda.spring.spel;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.dawnsci.nexus.template.NexusTemplate;
import org.eclipse.dawnsci.nexus.template.NexusTemplateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.yaml.snakeyaml.Yaml;

import gda.factory.Finder;
import gda.jython.InterfaceProvider;
import uk.ac.diamond.osgi.services.ServiceProvider;

public class NexusYamlTemplateSpELProcessor {

	private static final Logger logger = LoggerFactory.getLogger(NexusYamlTemplateSpELProcessor.class);
	private ExpressionParser parser;
	private StandardEvaluationContext context;
	private Map<String, List<Class<?>>> functionNamesParameterTypes = new HashMap<>();
	private List<String> nodeNameContainingSpELExpressions = new ArrayList<>();
	/**
	 * The SnakeYAML Yaml object, essentially a Facade to the snakeyaml API.
	 */
	private final Yaml yamlParser = new Yaml(); // note: not thread-safe, must only be called by synchronized methods

	public NexusYamlTemplateSpELProcessor() {
		//no-op used for Spring bean
	}

	public NexusTemplate process(String templateFilePath) throws IOException {
		final Path filePath = Paths.get(templateFilePath);
		Reader reader = Files.newBufferedReader(filePath);
		Map<String, Object>	yamlMap = load(reader);
		Map<String, Object> newMap = processMap(yamlMap);
		NexusTemplateService service = ServiceProvider.getService(NexusTemplateService.class);
		return service.createTemplate(filePath.toFile().getName(), newMap);
	}

	private synchronized Map<String, Object> load(Reader reader) {
		return yamlParser.load(reader);
	}

	/**
	 * process Spring expressions as value in the map and replace it by the result of expressions
	 *
	 * @param map
	 * @return map
	 */
	@SuppressWarnings("unchecked")
	private Map<String, Object> processMap(Map<String, Object> map) {
		logger.debug("Enter process SpEl expression {}", map);
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			if (entry.getValue() instanceof Map nestedMap) {
					if (nodeNameContainingSpELExpressions.contains(entry.getKey())) {
						processSPEL(nestedMap);
					}
					processMap(nestedMap);
			}
		}
		return map;
	}

	/**
	 * process Spring expression in the field value
	 *
	 * @param innerMap
	 */
	private void processSPEL(Map<String, Object> innerMap) {
		for (Map.Entry<String, Object> entry : innerMap.entrySet()) {
			if (entry.getValue() instanceof String val && val.startsWith("#{") && val.endsWith("}")) {
				val = val.substring(2, val.length() - 1);
				logger.debug("process SpEl expression {}", val);
				String value = parser.parseExpression(val).getValue(context, String.class);
				logger.debug("Result of SpEL expression is: {}", value);
				innerMap.put(entry.getKey(), value);
			}
		}
	}

	/**
	 * initialise SpEL parser, context, and register bean solver and functions in this context.
	 * This must be called after constructor call and function name and types being set!
	 * If XML bean is used, this should be wired into 'init-method' attribute.
	 *
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	public void init() throws NoSuchMethodException, SecurityException {
		parser = new SpelExpressionParser();
		context = new StandardEvaluationContext();
		context.setBeanResolver((ec, name) -> Finder.find(name) != null ? Finder.find(name) : InterfaceProvider.getJythonNamespace().getFromJythonNamespace(name));
		if (!functionNamesParameterTypes.isEmpty()) {
			// register function in SpELUtils class to be used
			for (Map.Entry<String, List<Class<?>>> e : functionNamesParameterTypes.entrySet()) {
				Class<?>[] parameterTypes = new Class[e.getValue().size()];
				Class<?>[] array = e.getValue().toArray(parameterTypes);
				context.registerFunction(e.getKey(), SpELUtils.class.getDeclaredMethod(e.getKey(), array));
			}
		}
	}

	public Map<String, List<Class<?>>> getFunctionNamesParameterTypes() {
		return functionNamesParameterTypes;
	}

	public void setFunctionNamesParameterTypes(Map<String, List<Class<?>>> functionNamesParameterTypes) {
		this.functionNamesParameterTypes = functionNamesParameterTypes;
	}

	public List<String> getNodeNameContainingSpELExpressions() {
		return nodeNameContainingSpELExpressions;
	}

	public void setNodeNameContainingSpELExpressions(List<String> nodeNameContainingSpELExpressions) {
		this.nodeNameContainingSpELExpressions = nodeNameContainingSpELExpressions;
	}
}
