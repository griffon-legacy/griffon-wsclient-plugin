
Dynamic WS client & libraries
-----------------------------

Plugin page: [http://artifacts.griffon-framework.org/plugin/wsclient](http://artifacts.griffon-framework.org/plugin/wsclient)


The Wsclient plugin adds a remoting client capable of communicating via SOAP.
It is compatible with [Grails' Xfire plugin 0.8.3][1].

Usage
-----
The plugin will inject the following dynamic methods:

 * `<R> R withWs(Map<String, Object> params, Closure<R> stmts)` - executes stmts
   using a WSClient
 * `<R> R withWs(Map<String, Object> params, CallableWithArgs<R> stmts)` - executes
   stmts using a WSClient

Where params may contain

| *Property*  | *Type*      | *Required* | *Notes*                                          |
| ----------- | ----------- | ---------- | ------------------------------------------------ |
| wsdl        | String      | yes        | WSDL location                                    |
| soapVersion | String      | no         | either "1.1" or "1.2". Defaults to "1.1"         |
| classLoader | ClassLoader | no         | classloader used for proxies classes             |
| timeout     | long        | no         |                                                  |
| mtom        | boolean     | no         | enable mtom                                      |
| basicAuth   | Map         | no         | must define values for `username` and `password` |
| proxy       | Map         | no         | proxy settings                                   |
| ssl         | Map         | no         | ssl settings                                     |

Keys for both `proxy` and `ssl` must match values from
`groovyx.net.ws.cxf.SettingsConstants` reproduced here for your convenience

    /** Http proxy user */
    public static final String HTTP_PROXY_USER = "http.proxy.user";
    /** Http proxy password */
    public static final String HTTP_PROXY_PASSWORD = "http.proxy.password";
    /** Http proxy host */
    public static final String HTTP_PROXY_HOST = "http.proxyHost";
    /** Http proxy port */
    public static final String HTTP_PROXY_PORT = "http.proxyPort";
    /** SSL truststore */
    public static final String HTTPS_TRUSTSTORE = "https.truststore";
    /** SSL truststore password */
    public static final String HTTPS_TRUSTSTORE_PASS = "https.truststore.pass";
    /** SSL keystore */
    public static final String HTTPS_KEYSTORE = "https.keystore";
    /** SSL keystore password */
    public static final String HTTPS_KEYSTORE_PASS = "https.keystore.pass";
    /** Http basic authentication user */
    public static final String HTTP_USER = "http.user";
    /** Http basic authentication password */
    public static final String HTTP_PASSWORD = "http.password";

All dynamic methods will create a new client when invoked unless you define an
`id:` attribute. When this attribute is supplied the client will be stored in
a cache managed by the `WsclientProvider` that handled the call.

These methods are also accessible to any component through the singleton
`griffon.plugins.wsclient.WsclientEnhancer`. You can inject these methods to
non-artifacts via metaclasses. Simply grab hold of a particular metaclass and
call `WsclientEnhancer.enhance(metaClassInstance)`.

Configuration
-------------
### WsclientAware AST Transformation

The preferred way to mark a class for method injection is by annotating it with
`@griffon.plugins.wsclient.WsclientAware`. This transformation injects the
`griffon.plugins.wsclient.WsclientContributionHandler` interface and default behavior
that fulfills the contract.

### Dynamic Method Injection

Dynamic methods will be added to controllers by default. You can
change this setting by adding a configuration flag in `griffon-app/conf/Config.groovy`

    griffon.ws.injectInto = ['controller', 'service']

Dynamic method injection will be skipped for classes implementing
`griffon.plugins.wsclient.WsclientContributionHandler`.

### Example

This example relies on [Grails][2] as the service provider. Follow these steps
to configure the service on the Grails side:

1. Download a copy of [Grails][3] and install it.
2. Create a new Grails application. We'll pick 'exporter' as the application name.

        grails create-app exporter

3. Change into the application's directory. Install the xfire plugin.

        grails install-plugin xfire

4. Create an implementation of the `Calculator` interface as a service

        grails create-service calculator

5. Paste the following code in `grails-app/services/exporter/CalculatorService.groovy`

        package exporter
        class CalculatorService {
            boolean transactional = false
            static expose = ['xfire']
        
            double add(double a, double b){
                println "add($a, $b)" // good old println() for quick debugging
                return a + b
            }
        }

6. Run the application

        grails run-app

Now we're ready to build the Griffon application

1. Create a new Griffon application. We'll pick `calculator` as the application name

        griffon create-app calculator
    
2. Install the wsclient plugin

        griffon install-plugin wsclient

3. Fix the view script to look like this

        package calculator
        application(title: 'Wsclient Plugin Example',
          pack: true,
          locationByPlatform: true,
          iconImage: imageIcon('/griffon-icon-48x48.png').image,
          iconImages: [imageIcon('/griffon-icon-48x48.png').image,
                       imageIcon('/griffon-icon-32x32.png').image,
                       imageIcon('/griffon-icon-16x16.png').image]) {
            gridLayout(cols: 2, rows: 4)
            label('Num1:')
            textField(columns: 20, text: bind(target: model, targetProperty: 'num1'))
            label('Num2:')
            textField(columns: 20, text: bind(target: model, targetProperty: 'num2'))
            label('Result:')
            label(text: bind{model.result})
            button('Calculate', enabled: bind{model.enabled}, actionPerformed: controller.calculate)
        }

4. Let's add required properties to the model

        package calculator
        @Bindable
        class CalculatorModel {
           String num1
           String num2
           String result
           boolean enabled = true
        }

5. Now for the controller code. Notice that there is minimal error handling in place. If the user
types something that is not a number the client will surely break, but the code is sufficient for now.

        package calculator
        @griffon.plugins.wsclient.WsclientAware
        class CalculatorController {
            def model
 
            def calculate = { evt = null ->
                double a = model.num1.toDouble()
                double b = model.num2.toDouble()
                execInsideUISync { model.enabled = false }
                try {
                    def result = withWs(wsdl: "http://localhost:8080/exporter/services/calculator?wsdl") {
                        add(a, b)
                    }
                    execInsideUIAsync { model.result = result.toString() }
                } finally {
                    execInsideUIAsync { model.enabled = true }
                }
            }
        }

6. Run the application

        griffon run-app

The plugin exposes a Java friendly API to make the exact same calls from Java,
or any other JVM language for that matter. Here's for example the previous code
rewritten in Java. Note the usage of @WsclientWare on a Java class

    package calculator;
    import griffon.util.CallableWithArgs;
    import griffon.util.CollectionUtils;
    import java.awt.event.ActionEvent;
    import java.util.Map;
    import groovyx.net.ws.WSClient;
    import org.codehaus.griffon.runtime.core.AbstractGriffonController;
    @griffon.plugins.wsclient.WsclientAware
    public class CalculatorController extends AbstractGriffonController {
        private CalculatorModel model;
    
        public void setModel(CalculatorModel model) {
            this.model = model;
        }
    
        public void calculate(ActionEvent event) {
            final double a = Double.parseDouble(model.getNum1());
            final double b = Double.parseDouble(model.getNum2());
            enableModel(false);
            try {
                Map<String, Object> params = CollectionUtils.<String, Object> map()
                    .e("wsdl", "http://localhost:8080/exporter/services/calculator?wsdl");
                final Double result = withWs(params,
                    new CallableWithArgs<Double>() {
                        public Double call(Object[] args) {
                            WSClient client = (WSClient) args[0];
                            Number n = (Number) client.invokeMethod("add", new Object[] { a, b });
                            return n.doubleValue();
                        }
                    });
                execInsideUIAsync(new Runnable() {
                    public void run() {
                        model.setResult(String.valueOf(result));
                    }
                });
            } finally {
                enableModel(true);
            }
        }
    
        private void enableModel(final boolean enabled) {
            execInsideUIAsync(new Runnable() {
                public void run() {
                    model.setEnabled(enabled);
                }
            });
        }
    }

Testing
-------

Dynamic methods will not be automatically injected during unit testing, because
addons are simply not initialized for this kind of tests. However you can use
`WsclientEnhancer.enhance(metaClassInstance, wsclientProviderInstance)` where
`wsclientProviderInstance` is of type `griffon.plugins.wsclient.WsclientProvider`.
The contract for this interface looks like this

    public interface WsclientProvider {
        <R> R withWs(Map<String, Object> params, Closure<R> closure);
        <R> R withWs(Map<String, Object> params, CallableWithArgs<R> callable);
    }

It's up to you define how these methods need to be implemented for your tests.
For example, here's an implementation that never fails regardless of the
arguments it receives

    class MyWsclientProvider implements WsclientProvider {
        public <R> R withWs(Map<String, Object> params, Closure<R> closure) { null }
        public <R> R withWs(Map<String, Object> params, CallableWithArgs<R> callable) { null }
    }
    
This implementation may be used in the following way

    class MyServiceTests extends GriffonUnitTestCase {
        void testSmokeAndMirrors() {
            MyService service = new MyService()
            WsclientEnhancer.enhance(service.metaClass, new MyWsclientProvider())
            // exercise service methods
        }
    }

On the other hand, if the service is annotated with `@WsclientAware` then usage
of `WsclientEnhancer` should be avoided at all costs. Simply set
`wsclientProviderInstance` on the service instance directly, like so, first the
service definition

    @griffon.plugins.wsclient.WsclientAware
    class MyService {
        def serviceMethod() { ... }
    }

Next is the test

    class MyServiceTests extends GriffonUnitTestCase {
        void testSmokeAndMirrors() {
            MyService service = new MyService()
            service.wsclientProvider = new MyWsclientProvider()
            // exercise service methods
        }
    }

Tool Support
------------

### DSL Descriptors

This plugin provides DSL descriptors for Intellij IDEA and Eclipse (provided
you have the Groovy Eclipse plugin installed). These descriptors are found
inside the `griffon-wsclient-compile-x.y.z.jar`, with locations

 * dsdl/wsclient.dsld
 * gdsl/wsclient.gdsl

### Lombok Support

Rewriting Java AST in a similar fashion to Groovy AST transformations is
possible thanks to the [lombok][4] plugin.

#### JavaC

Support for this compiler is provided out-of-the-box by the command line tools.
There's no additional configuration required.

#### Eclipse

Follow the steps found in the [Lombok][4] plugin for setting up Eclipse up to
number 5.

 6. Go to the path where the `lombok.jar` was copied. This path is either found
    inside the Eclipse installation directory or in your local settings. Copy
    the following file from the project's working directory

         $ cp $USER_HOME/.griffon/<version>/projects/<project>/plugins/wsclient-<version>/dist/griffon-wsclient-compile-<version>.jar .

 6. Edit the launch script for Eclipse and tweak the boothclasspath entry so
    that includes the file you just copied

        -Xbootclasspath/a:lombok.jar:lombok-pg-<version>.jar:        griffon-lombok-compile-<version>.jar:griffon-wsclient-compile-<version>.jar

 7. Launch Eclipse once more. Eclipse should be able to provide content assist
    for Java classes annotated with `@WsclientAware`.

#### NetBeans

Follow the instructions found in [Annotation Processors Support in the NetBeans
IDE, Part I: Using Project Lombok][5]. You may need to specify
`lombok.core.AnnotationProcessor` in the list of Annotation Processors.

NetBeans should be able to provide code suggestions on Java classes annotated
with `@WsclientAware`.

#### Intellij IDEA

Follow the steps found in the [Lombok][4] plugin for setting up Intellij IDEA
up to number 5.

 6. Copy `griffon-wsclient-compile-<version>.jar` to the `lib` directory

         $ pwd
           $USER_HOME/Library/Application Support/IntelliJIdea11/lombok-plugin
         $ cp $USER_HOME/.griffon/<version>/projects/<project>/plugins/wsclient-<version>/dist/griffon-wsclient-compile-<version>.jar lib

 7. Launch IntelliJ IDEA once more. Code completion should work now for Java
    classes annotated with `@WsclientAware`.


[1]: http://grails.org/plugin/wsclient
[2]: http://grails.org
[3]: http://grails.org/Download
[4]: /plugin/lombok
[5]: http://netbeans.org/kb/docs/java/annotations-lombok.html

### Building

This project requires all of its dependencies be available from maven compatible repositories.
Some of these dependencies have not been pushed to the Maven Central Repository, however you
can obtain them from [lombok-dev-deps][lombok-dev-deps].

Follow the instructions found there to install the required dependencies into your local Maven
repository before attempting to build this plugin.

[lombok-dev-deps]: https://github.com/aalmiray/lombok-dev-deps