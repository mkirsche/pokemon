package generator;

class MethodFormatter {

    private int tabs;
    private int parenthesesBalance;
    private boolean inSwitch;
    private boolean inCases;

    MethodFormatter(int tabs) {
        this.tabs = tabs;

        this.inSwitch = false;
        this.inCases = false;
    }

    void appendLine(String line, StringBuilder method) {

        if (line.startsWith("switch (")) {
            inSwitch = true;
        }

        if (inSwitch) {
            boolean inBefore = inCases;
            inCases = line.startsWith("case ") || line.equals("default:");

            if (inBefore && !inCases) {
                tabs++;
            }
        }

        if (line.contains("}") && !line.contains("{")) {
            tabs--;
            inSwitch = false;
        }

        int numOpen = (int)line.chars().filter(num -> num == '(').count();
        int numClosed = (int)line.chars().filter(num -> num == ')').count();
        boolean previouslyInParentheses = parenthesesBalance > 0;
        parenthesesBalance += numOpen - numClosed;
        boolean nowInParentheses = parenthesesBalance > 0;

        if (previouslyInParentheses && !nowInParentheses) {
            tabs--;
        }

        // Add the tabs
        for (int i = 0; i < tabs; i++) {
            method.append("\t");
        }

        // Actually write the line
        method.append(line)
                .append("\n");

        if (inSwitch && (line.equals("break;") || line.startsWith("return ") || line.equals("return;"))) {
            tabs--;
        }

        if (line.contains("{") && !line.contains("}")) {
            tabs++;
        }

        if (!previouslyInParentheses && nowInParentheses) {
            tabs++;
        }
    }
}
