import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;

public class SequenceDiagram   {

    static final String STANDARD_SEQUENCE_FILE = "sequencediagram.svg";
    static final String DELEGATED_SEQUENCE_FILE = "delegateddiagram.svg";

    static final double ACTOR_TOP_Y = 32;
    static final double ACTOR_HEIGHT = 40;

    static final double PROC_WIDTH = 86;
    static final double PROC_SLANT = 8;
    static final double PROC_HEIGHT = 22;
    static final String PROC_COLOR = "#faf2dd";
    static final String PROC_STROKE = "#404040";

    static final double VBAR_WIDTH = 1.2;
    static final double VBAR_START_Y = ACTOR_TOP_Y + ACTOR_HEIGHT - 5;
    static final double VBAR_X_DISTANCE = 120;
    static final double VBAR_START_X = PROC_WIDTH / 2 + 20;
    static final String VBAR_COLOR = "black";

    static final double NUMB_WIDTH = 14;

    static final double STROKE_WIDTH = 0.5;

    static final double ARROW_WIDTH = 0.8;
    static final double ARROW_HEAD_WIDTH = 5;
    static final double ARROW_HEAD_LENGTH = 7;
    static final double ARROW_HEAD_GUTTER = 0.5;
    static final String ARROW_COLOR = "#cc1010";
    static final String ARROW_RETURN_COLOR = "#009000";

    static final double DROP_OFFSET = 2;

    static final int FONT_SIZE = 12;
    static final double FONT_OFFSET = 4.3;

    static final double HEADER_Y = 20;

    static final double UI_WIDTH = 86;
    static final double UI_HEIGHT = 50;
    static final double UI_BORDER = 5;
 
    static final String CLICKABLE_COLOR = "#4366ff";

    static final double SEQ_Y_DISTANCE = 32;
    static final double SEQ_Y_SLACK_AFTER_UI = 4;

    static final double SCOPE_Y_MARGIN = 15;
    static final double SCOPE_X_MARGIN = 25;

    static final String FONT_FAMILY = "var(--font-sans)";

    String originalBase;

    double seqY = 100;

    int actorPos = 0;

    StringBuilder svg = new StringBuilder ();

    int currNum = 1;

    boolean delegatedPart = false;  // For reference generation

    void number(double centerX, double centerY) {
        int n = currNum++;
        double width = NUMB_WIDTH;
        double fontX = centerX;
        if (n > 9) {
            width *= 1.3;
        }
        if (n > 9 && n != 11 &&  n < 20) {
            fontX -= FONT_SIZE / 15.0;
        }
        String numText = String.valueOf(n);
        if (delegatedPart) {
            numText = "D" + numText;
            width += NUMB_WIDTH *.5;
        }
        svg.append("  <a href='#seq-")
           .append(numText)
           .append("'>\n  <rect x='")
           .append(convert(centerX - width / 2))
           .append("' y='")
           .append(convert(centerY - NUMB_WIDTH / 2))
           .append("' width='")
           .append(convert(width))
           .append("' height='")
           .append(convert(NUMB_WIDTH))
           .append("' rx='3' fill='white' stroke='" + PROC_STROKE + "' stroke-width='")
           .append(convert(STROKE_WIDTH))
           .append("'/>\n");
        _text(fontX, centerY, numText, FONT_SIZE, CLICKABLE_COLOR);
        svg.append("  </a>\n");
    }

    void text(double centerX, double centerY, String text) {
        _text(centerX, centerY, text, FONT_SIZE, "black");
    }

    void _text(double centerX, double centerY, String text, double fontSize, String color) {
        svg.append("  <text x='")
           .append(convert(centerX))
           .append("' y='")
           .append(convert(centerY + FONT_OFFSET))
           .append("' fill='")
           .append(color)
           .append("' font-family='" + FONT_FAMILY + "' font-size='")
           .append(convert(fontSize))
           .append("' text-anchor='middle'>")
           .append(text)
           .append("</text>\n");
    }

    void processing(int vbar) {
        seqY -= SEQ_Y_SLACK_AFTER_UI;
        double rectY = seqY - (PROC_HEIGHT + STROKE_WIDTH) / 2;
        double startX = center(vbar) - PROC_WIDTH / 2 + PROC_SLANT / 2;
        svg.append("  <path fill='" + PROC_COLOR + "' stroke='" + PROC_STROKE + "' stroke-width='0.5' d='M")
           .append(convert(startX))
           .append(',')
           .append(convert(rectY))
           .append(" l")
           .append(convert(PROC_WIDTH))
           .append(',')
           .append("0 l")
           .append(convert(-PROC_SLANT))
           .append(',')
           .append(convert(PROC_HEIGHT))
           .append(" l")
           .append(convert(-PROC_WIDTH))
           .append(",0Z' filter='url(#dropShaddow)'/>\n");
        text(center(vbar) + NUMB_WIDTH / 5, seqY - STROKE_WIDTH / 2, "Processing");
        number(startX - PROC_SLANT / 2 - STROKE_WIDTH / 2, seqY - STROKE_WIDTH / 2);
        seqY += SEQ_Y_DISTANCE + SEQ_Y_SLACK_AFTER_UI;
    }

    void dashedArrow(int from, int to, String label) {
        _arrow(from, to, label, " stroke-dasharray='4'", false);
    }

    void returnArrow(int from, int to, String label) {
        _arrow(from, to, label, "", true);
    }

    void arrow(int from, int to, String label) {
        _arrow(from, to, label, "", false);
    }

    void _arrow(int from, int to, String label, String additional, boolean returnData) {
        double startX = (to > from ? VBAR_WIDTH : -VBAR_WIDTH) / 2;
        double endX = to > from ? -ARROW_HEAD_LENGTH - ARROW_HEAD_GUTTER : ARROW_HEAD_LENGTH + ARROW_HEAD_GUTTER;
        String arrowY = convert(seqY);
        double textX = (center(to) + center(from)) / 2;
        if (Math.abs(to - from) > 1) {
            textX = (center(from + 1) + center(from)) / 2;
        }
        svg.append("  <line x1='")
           .append(convert(center(from) + startX))
           .append("' y1='")
           .append(arrowY)
           .append("' x2='")
           .append(convert(center(to) + endX - startX))
           .append("' y2='")
           .append(arrowY)
           .append("' stroke-width='")
           .append(convert(ARROW_WIDTH))
           .append("' stroke='")
           .append(returnData ? ARROW_RETURN_COLOR : ARROW_COLOR)
           .append("' marker-end='url(#")
           .append(returnData ? "arrowReturnHead" : "arrowHead")
           .append(")'")
           .append(additional)
           .append("/>\n");
        text(textX, seqY - FONT_SIZE * 0.8, label);
        number(center(from), seqY);
        seqY += SEQ_Y_DISTANCE;
    }

    double center(int i) {
        return VBAR_START_X + i * VBAR_X_DISTANCE;
    }

    String embedded;

    double findArgument(String property) throws Exception {
        char delim = '"';
        int start = embedded.indexOf(property + "=\"");
        if (start < 0) {
            start = embedded.indexOf(property + "='");
            if (start < 0) {
                throw new IOException("Did not find: " + property);
            }
            delim = '\''; 
        }
        int valueStart = start + property.length() + 2;
        int valueStop = embedded.indexOf(delim, valueStart);
        double argument = Double.valueOf(embedded.substring(valueStart, valueStop));
        embedded = embedded.substring(0, start) + "\u0000" + embedded.substring(valueStop + 1);
        return argument;
    }
    void actor(String fileName, String label) throws Exception {
        int vbar = actorPos++;
        embedded = readOriginal(fileName).replace('"', '\'');
        double width = findArgument("width");
        double height = findArgument("height");
        int start = embedded.indexOf('\u0000');
        int stop = embedded.lastIndexOf('\u0000');
        embedded = embedded.substring(0, start) + "viewBox='0 0 " + convert(width) + " " + convert(height) + "'" +
            embedded.substring(stop + 1);
         double embeddedWidth = ACTOR_HEIGHT * width / height;
        svg.append("<svg x='")
           .append(convert(center(vbar) - embeddedWidth / 2))
           .append("' y='")
           .append(ACTOR_TOP_Y)
           .append("' width='")
           .append(convert(embeddedWidth))
           .append("' height='")
           .append(ACTOR_HEIGHT)
           .append("'")
          .append(embedded.substring(embedded.indexOf("<svg") + 4));
        _text(center(vbar), HEADER_Y, label, FONT_SIZE * 1.2, "black");

     }

    SequenceDiagram  (String originalBase, boolean standardSequenceDiagram) throws Exception {
        this.originalBase = originalBase;
        if (standardSequenceDiagram) {
            actor("user.svg",     "User");
            actor("browser.svg",  "Browser");
        }
        actor("merchant.svg", "Merchant");
        actor("psp.svg",      "PSP");
        if (!standardSequenceDiagram) {
            actor("authorizer.svg", "Authorizer");
        }
        actor("issuer.svg",   "Issuer");

        if (standardSequenceDiagram) {

            double scopeRectY = seqY - SEQ_Y_DISTANCE + SCOPE_Y_MARGIN + SEQ_Y_SLACK_AFTER_UI;
            seqY += SCOPE_Y_MARGIN;
            arrow(2, 1, "PaymentRequest");

            seqY -= SEQ_Y_SLACK_AFTER_UI;
            double uiY = seqY + (UI_HEIGHT - PROC_HEIGHT) / 2;
            double uiTop = uiY - (UI_HEIGHT + STROKE_WIDTH) / 2;
            double uiX = center(1) - (UI_WIDTH + STROKE_WIDTH) / 2;
            svg.append("  <rect x='")
               .append(convert(uiX))
               .append("' y='")
               .append(convert(uiTop))
               .append("' width='")
               .append(convert(UI_WIDTH))
               .append("' height='")
               .append(convert(UI_HEIGHT))
               .append("' fill='white' stroke-width='")
               .append(convert(STROKE_WIDTH))
               .append("' stroke='black' filter='url(#dropShaddow)'/>\n")
               .append("  <rect x='")
               .append(convert(uiX))
               .append("' y='")
               .append(convert(uiTop))
               .append("' width='")
               .append(convert(UI_WIDTH))
               .append("' height='")
               .append(convert(UI_BORDER))
               .append("' fill='#deeafc' stroke-width='")
               .append(convert(STROKE_WIDTH))
               .append("' stroke='black'/>\n");  
            text(center(1), uiY, "Payment UI");
            number(uiX, uiTop + UI_BORDER / 2);
            seqY += SEQ_Y_DISTANCE + SEQ_Y_SLACK_AFTER_UI + UI_HEIGHT - PROC_HEIGHT;
            dashedArrow(0, 1, "Authorization");
            processing(1);
            seqY += SCOPE_Y_MARGIN + SEQ_Y_SLACK_AFTER_UI;
            double scopeRectHeight = seqY - scopeRectY - SEQ_Y_DISTANCE + SEQ_Y_SLACK_AFTER_UI;
            arrow(1, 2, "FWP Assertion");
            processing(2);
            arrow(2, 3, "PSPRequest");
            processing(3);
            arrow(3, 4, "IssuerRequest");
            processing(4);
            svg.append("  <rect x='")
               .append(convert(center(0) - SCOPE_X_MARGIN))
               .append("' y='")
               .append(convert(scopeRectY))
               .append("' width='")
               .append(convert(center(2) - center(0) + 2 * SCOPE_X_MARGIN))
               .append("' height='")
               .append(convert(scopeRectHeight))
               .append("' rx='5' fill='none' stroke-width='0.5' stroke='black' stroke-dasharray='1'/>\n");
        } else {
            currNum = 7;
            arrow(0, 1, "PSPRequest");
            currNum = 1;
            delegatedPart = true;
            processing(1);
            arrow(1, 2, "&#160;&#160;AuthorizeRequest");
            processing(2);
            returnArrow(2, 1, "Authorization");
            processing(1);
            arrow(1, 3, "&quot;Payment Rails&quot;");
            processing(3);
      }
  //      dashedArrow(4, 3, "Out of Scope", "#outofscope");
 
        seqY -= PROC_HEIGHT * 0.5;
        StringBuilder preludium = new StringBuilder("<?xml version='1.0' encoding='utf-8'?>\n" +
                                                    "<svg viewBox='0 0 ")
            .append(convert(center(actorPos - 1) + PROC_WIDTH / 2 + 20))
            .append(" ")
            .append(convert(seqY + 10))
            .append("' xmlns='http://www.w3.org/2000/svg'>\n" +
  "    <title>FIDO Web Pay - Sample")
            .append(standardSequenceDiagram ? "" : " Delegated")
            .append(" Sequence Diagram</title>\n" +
  "    <!-- Anders Rundgren 2021 -->\n" +
  "    <defs>\n" +
  "    <filter id='dropShaddow'>\n" +
  "      <feDropShadow dx='1.5' dy='1.5' stdDeviation='1.5' flood-opacity='0.4'/>\n" +
  "    </filter>\n")
            .append("    <marker id='arrowHead' markerWidth='")
            .append(convert(ARROW_HEAD_LENGTH))
            .append("' markerHeight='")
            .append(convert(ARROW_HEAD_WIDTH))
            .append("' refX='0' refY='")
            .append(convert(ARROW_HEAD_WIDTH / 2))
            .append("' orient='auto' markerUnits='userSpaceOnUse'>\n" +
            "      <polygon points='0 0, ")
            .append(convert(ARROW_HEAD_LENGTH))
            .append(" ")
            .append(convert(ARROW_HEAD_WIDTH / 2))
            .append(", 0 ")
            .append(convert(ARROW_HEAD_WIDTH))
            .append("' fill='" + ARROW_COLOR + "'/>\n" +
                    "    </marker>\n" +
                    "    <marker id='arrowReturnHead' markerWidth='")
            .append(convert(ARROW_HEAD_LENGTH))
            .append("' markerHeight='")
            .append(convert(ARROW_HEAD_WIDTH))
            .append("' refX='0' refY='")
            .append(convert(ARROW_HEAD_WIDTH / 2))
            .append("' orient='auto' markerUnits='userSpaceOnUse'>\n" +
            "      <polygon points='0 0, ")
            .append(convert(ARROW_HEAD_LENGTH))
            .append(" ")
            .append(convert(ARROW_HEAD_WIDTH / 2))
            .append(", 0 ")
            .append(convert(ARROW_HEAD_WIDTH))
            .append("' fill='" + ARROW_RETURN_COLOR + "'/>\n" +
               "    </marker>\n" +
               "  </defs>\n");
        for (int i = 0; i < actorPos; i++) {
            String x = convert(i * VBAR_X_DISTANCE + VBAR_START_X);
            preludium.append("  <line x1='")
                 .append(x)
                 .append("' y1='")
                 .append(convert(VBAR_START_Y))
                 .append("' x2='")
                 .append(x)
                 .append("' y2='")
                 .append(convert(seqY))
                 .append("' stroke-width='")
                 .append(convert(VBAR_WIDTH))
                 .append("' stroke='" + VBAR_COLOR + "'/>\n");
        }
        svg.insert(0, preludium);

        new FileOutputStream(originalBase + 
            (standardSequenceDiagram ?
              STANDARD_SEQUENCE_FILE : DELEGATED_SEQUENCE_FILE)).write(svg.append("</svg>")
                .toString().getBytes("utf-8"));
     }

    static byte[] getByteArrayFromInputStream(InputStream is) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(10000);
        byte[] buffer = new byte[10000];
        int bytes;
        while ((bytes = is.read(buffer)) != -1) {
            baos.write(buffer, 0, bytes);
        }
        is.close();
        return baos.toByteArray();
    }

    String readOriginal(String name) throws Exception {
        return new String(getByteArrayFromInputStream(new FileInputStream(originalBase + name)), "utf-8");
    }

    String convert(double d) {
        String s = Double.toString(d);
        return s.endsWith(".0") ? s.substring(0, s.length() - 2) : s; 
    }

    public static void main(String[] argc) {
        try {
            new SequenceDiagram(argc[0] + File.separator, Boolean.valueOf(argc[1]));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
