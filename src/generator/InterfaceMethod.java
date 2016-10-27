package generator;

import battle.Battle;
import generator.InvokeMethod.CheckGetInvoke;
import generator.InvokeMethod.CheckInvoke;
import generator.InvokeMethod.ContainsInvoke;
import generator.InvokeMethod.VoidInvoke;
import main.Global;
import util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

class InterfaceMethod {

    private enum InvokeType {
        VOID(VoidInvoke::new),
        CONTAINS(ContainsInvoke::new),
        CHECK(CheckInvoke::new),
        CHECKGET(CheckGetInvoke::new);

        private final GetInvokeMethod getInvokeMethod;

        InvokeType(final GetInvokeMethod getInvokeMethod) {
            this.getInvokeMethod = getInvokeMethod;
        }

        private interface GetInvokeMethod {
            InvokeMethod getInvokeMethod(Scanner invokeInput);
        }

        public InvokeMethod getInvokeMethod(final Scanner invokeInput) {
            return this.getInvokeMethod.getInvokeMethod(invokeInput);
        }
    }

    private static final String COMMENTS = "Comments";
    private static final String RETURN_TYPE = "ReturnType";
    private static final String METHOD_NAME = "MethodName";
    private static final String HEADER = "Header";
    private static final String PARAMETERS = "Parameters";
    private static final String INVOKE_PARAMETERS = "InvokeParameters";
    private static final String INVOKE = "Invoke";
    private static final String INVOKE_NAME = "InvokeName";
    private static final String EFFECT_LIST = "EffectList";
    private static final String INVOKE_ATTACK = "InvokeAttack";
    private static final String SET_INVOKEES = "SetInvokees";
    private static final String MOVE = "Move";
    private static final String MOLD_BREAKER = "MoldBreaker";
    private static final String DEADSIES = "Deadsies";

    private final String interfaceName;

    private String returnType;
    private String methodName;

    private String parameters;
    private String typelessParameters;
    private String additionalInvokeParameters;
    private String battleParameter;

    private String invokeeDeclaration;

    private String moldBreaker;
    private List<String> deadsies;

    private String comments;
    private InvokeMethod invokeMethod;

    InterfaceMethod(final String interfaceName, Map<String, String> fields) {
        this.interfaceName = interfaceName;

        this.parameters = StringUtils.empty();
        this.typelessParameters = StringUtils.empty();
        this.deadsies = new ArrayList<>();

        this.readFields(fields);
    }

    private void readFields(Map<String, String> fields) {

        final String header = getField(fields, HEADER);
        if (header != null) {
            int openParenthesis = header.indexOf('(');
            int closeParenthesis = header.indexOf(')');

            if (openParenthesis == -1 || closeParenthesis == -1 || openParenthesis > closeParenthesis) {
                Global.error("Header must contain proper parentheses around parameters. " +
                        header + " is not valid.  Interface: " + this.interfaceName);
            }

            this.parameters = header.substring(openParenthesis + 1, closeParenthesis);

            String[] split = header.substring(0, openParenthesis).split(" ");
            if (split.length != 2) {
                Global.error("Header must have exactly two words -- return type and method name -- before parameter parentheses. " +
                        header + " is not valid.  Interface: " + this.interfaceName);
            }

            this.returnType = split[0];
            this.methodName = split[1];
        }

        final String returnType = getField(fields, RETURN_TYPE);
        if (returnType != null) {
            if (!StringUtils.isNullOrEmpty(this.returnType) || !StringUtils.isNullOrEmpty(this.methodName)) {
                Global.error("Cannot set the return type manually if it has already be set via the header field. " +
                        "Header Return Type: " + this.returnType + ", Header Method Name: " + this.methodName +
                        "New Return Type: " + returnType + ", Interface Name: " + this.interfaceName);
            }

            final String methodName = getField(fields, METHOD_NAME);
            if (methodName == null) {
                Global.error("Return type and method name must be specified together. " +
                        "Return Type: " + returnType + ", Interface Name: " + this.interfaceName);
            }

            this.returnType = returnType;
            this.methodName = methodName;
        }

        final String parameters = getField(fields, PARAMETERS);
        if (parameters != null) {
            this.parameters = parameters;
        }

        final String invokeParameters = getField(fields, INVOKE_PARAMETERS);
        if (invokeParameters != null){
            this.additionalInvokeParameters = invokeParameters;
        }

        this.setParameters();

        final String comments = getField(fields, COMMENTS);
        if (comments != null) {
            this.comments = comments;
        }

        final String invoke = getField(fields, INVOKE);
        if (invoke != null) {
            Scanner in = new Scanner(invoke);
            this.invokeMethod = InvokeType.valueOf(in.next().toUpperCase()).getInvokeMethod(in);
        }

        final String invokeName = getField(fields, INVOKE_NAME);
        if (invokeName != null) {
            if (this.invokeMethod == null) {
                Global.error("Must specify type of invoke method if you want to name it.  Interface: " + this.interfaceName);
            }

            this.invokeMethod.setMethodName(invokeName);
        }

        final String effectListParameter = getField(fields, EFFECT_LIST);
        if (effectListParameter != null) {
            this.invokeeDeclaration = String.format("List<Object> invokees = %s.getEffectsList(%s);",
                    this.battleParameter, effectListParameter);

            final String invokeAttack = getField(fields, INVOKE_ATTACK);
            if (invokeAttack != null) {
                this.invokeeDeclaration += "\ninvokees.add(" + invokeAttack + ".getAttack());\n";
            }
        }

        final String setInvokees = getField(fields, SET_INVOKEES);
        if (setInvokees != null) {
            if (!StringUtils.isNullOrEmpty(this.invokeeDeclaration)) {
                Global.error("Can not define multiple ways to set the effects list. " +
                        "Interface: " + this.interfaceName);
            }

            this.invokeeDeclaration = setInvokees + "\n";
        }

        // TODO: Eventually would just like to remove the invokee loop for this case and just operate directly on the attack
        final String moveInvoke = getField(fields, MOVE);
        if (moveInvoke != null) {
            if (!StringUtils.isNullOrEmpty(this.invokeeDeclaration)) {
                Global.error("Can not define multiple ways to set the effects list. " +
                        "Interface: " + this.interfaceName);
            }

            this.invokeeDeclaration = String.format("List<Object> invokees = " +
                    "Collections.singletonList(%s.getAttack());", moveInvoke);
        }

        final String moldBreaker = getField(fields, MOLD_BREAKER);
        if (moldBreaker != null) {
            this.moldBreaker = moldBreaker;
        }

        final String allDeadsies = getField(fields, DEADSIES);
        if (allDeadsies != null) {
            Scanner in = new Scanner(allDeadsies);
            while (in.hasNext()) {
                this.deadsies.add(in.next());
            }
        }

        for (final Entry<String, String> field : fields.entrySet()) {
            Global.error("Unused field " + field.getKey() + ": " + field.getValue() +
                    " for interface " + this.interfaceName);
        }

        if ((this.returnType == null || this.methodName == null) && this.invokeMethod == null) {
            Global.error("Interface method and invoke method are both missing for interface " + this.interfaceName);
        }
    }

    private void setParameters() {
        if (!StringUtils.isNullOrEmpty(this.parameters)) {
            final String[] split = this.parameters.split(",");
            for (final String typedParameter : split) {
                final String[] typeSplit = typedParameter.trim().split(" ");
                if (typeSplit.length != 2) {
                    Global.error("Should be exactly one space between split parameters. " +
                            "Parameters: " + this.parameters + ", Interface Name: " + this.interfaceName);
                }

                final String parameterType = typeSplit[0];
                final String parameterName = typeSplit[1];

                if (!this.typelessParameters.isEmpty()) {
                    this.typelessParameters += ", ";
                }

                this.typelessParameters += parameterName;

                if (parameterType.equals(Battle.class.getSimpleName())) {
                    if (!StringUtils.isNullOrEmpty(this.battleParameter)) {
                        Global.error("Can only have one battle parameter.  Interface: " + this.interfaceName);
                    }

                    this.battleParameter = parameterName;
                }
            }
        }

        if (StringUtils.isNullOrEmpty(this.battleParameter)) {
            this.battleParameter = "b";
        }
    }

    private static String getField(final Map<String, String> fields, final String key) {
        if (fields.containsKey(key)) {
            final String value = fields.get(key).trim();
            fields.remove(key);
            return value;
        }

        return null;
    }

    String writeInterfaceMethod() {
        if (StringUtils.isNullOrEmpty(this.returnType) || StringUtils.isNullOrEmpty(this.methodName)) {
            return StringUtils.empty();
        }

        final StringBuilder interfaceMethod = new StringBuilder();
        if (!StringUtils.isNullOrEmpty(this.comments)) {
            StringUtils.appendLine(interfaceMethod, "\n\t\t" + this.comments);
        }

        interfaceMethod.append(String.format("\t\t%s;\n", this.getHeader()));
        return interfaceMethod.toString();
    }

    String writeInvokeMethod() {
        if (this.invokeMethod == null) {
            return StringUtils.empty();
        }

        return this.invokeMethod.writeInvokeMethod(this);
    }

    private String getHeader() {
        return MethodInfo.createHeader(this.returnType, this.methodName, this.parameters);
    }

    String getMethodCall() {
        return MethodInfo.createHeader(this.methodName, this.typelessParameters);
    }

    String getInterfaceName() {
        return this.interfaceName;
    }

    String getParameters() {
        return this.parameters;
    }

    String getAdditionalInvokeParameters() {
        return this.additionalInvokeParameters;
    }

    String getInvokeeDeclaration() {
        return this.invokeeDeclaration;
    }

    String getMoldBreaker() {
        return this.moldBreaker;
    }

    String getBattleParameter() {
        return this.battleParameter;
    }

    Iterable<String> getDeadsies() {
       return this.deadsies;
    }
}
