import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.DataBufferInt;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.lang.StringBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import de.theappguys.depixelizer.algorithm.BackgroundTask;
import de.theappguys.depixelizer.algorithm.SimilarityGraph;
import de.theappguys.depixelizer.algorithm.PixelGraph;
import de.theappguys.depixelizer.algorithm.SvgRenderer;

public class Depixelizer {

    private static String clearLine = "\r                                \r";

    private static class ProgressFeedback implements BackgroundTask {
    
        public void energyOptimizationProgress(float progress) {
            System.err.print(clearLine + String.format("Smoothing... [%d%%]", Math.round(progress*100)));
        }

        public boolean isCancelled() {
            return false;
        }

    }

    /* Combine paths from SvgRenderer's output to take advantage of the even-odd rule.
     * We create a bucket for all sets of path attributes ignoring 'd'.
     * Then we concatenate the 'd' attributes within each bucket. */
    private static String combinePaths(String svg) throws IOException {
        StringBuilder output = new StringBuilder();
        BufferedReader br = new BufferedReader(new StringReader(svg));
        ArrayList<String> al = new ArrayList<String>();
        HashMap<String, StringBuilder> hm = new HashMap<String, StringBuilder>();
        Pattern p = Pattern.compile("(.* d=\")([^\"]*)\" />");
        String line = br.readLine();
        while(line != null && ! line.startsWith("<path")) {
            output.append(line);
            output.append("\n");
            line = br.readLine();
        }
        while(line != null && line.startsWith("<path")) {
            Matcher m = p.matcher(line);
            if(!m.matches()) throw new IOException("Unexpected path in SVG");
            String path = m.group(1);
            String data = m.group(2);
            StringBuilder sb = hm.get(path);
            if(sb != null) {
                sb.append(" ");
            } else {
                sb = new StringBuilder();
                hm.put(path, sb);
                al.add(path);
            }
            sb.append(data);
            line = br.readLine();
        }
        for(String path: al) {
            StringBuilder data = hm.get(path);
            output.append(path);
            if(data != null) output.append(data);
            output.append("\" />\n");
        }
        while(line != null) {
            output.append(line);
            output.append("\n");
            line = br.readLine();
        }
        return output.toString();
    }
    
    public static void main(String[] args) throws IOException {
        String infile = null, outfile = null;
        boolean smooth = true, combine = true, valid = false;

        for(String arg: args) {
            if(arg.equals("-s")) {
                smooth = false;
            } else if(arg.equals("-c")) {
                combine = false;
            } else if(infile == null) {
                infile = arg;
                valid = true;
            } else if(outfile == null) {
                outfile = arg;
            } else {
                valid = false;
            }
        }

        if(!valid) {
            System.err.println("Usage: depix [-s] [-c] input.png [output.svg]");
            System.err.println("         -s: skip polygon smoothing");
            System.err.println("         -c: skip combining paths");
            System.exit(1);
        }

        if(outfile == null) {
            outfile = infile.replaceFirst("\\.[^.]*$", "") + ".svg";
        }

        System.err.print("Reading image...");
	System.setProperty("java.awt.headless", "true");
        BufferedImage image = ImageIO.read(new File(infile));
        int width = image.getWidth();
        int height = image.getHeight();

        BufferedImage argbImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        ColorConvertOp ccop = new ColorConvertOp(image.getColorModel().getColorSpace(),
                                             argbImage.getColorModel().getColorSpace(), null);
        ccop.filter(image, argbImage);
        DataBufferInt pixbuf = (DataBufferInt) argbImage.getRaster().getDataBuffer();

        SimilarityGraph similarityGraph = new SimilarityGraph(width, height);
        System.arraycopy(pixbuf.getData(), 0, similarityGraph.getRgbaColors(), 0, width * height);

        System.err.print(clearLine + "Converting colors...");
        similarityGraph.colorConversion(similarityGraph.getRgbaColors());

        System.err.print(clearLine + "Building similarity graph..");
        similarityGraph.buildGraph();

        System.err.print(clearLine + "Removing conflicts...");
        similarityGraph.removeConflicts();

        System.err.print(clearLine + "Creating polygons...");
        PixelGraph pixelGraph = new PixelGraph();
        similarityGraph.fillPixelGraph(pixelGraph);

        if(smooth) {

            System.err.print(clearLine + "Assembling splines...");
            pixelGraph.assembleSplines();

            System.err.print(clearLine + "Smoothing...");
            pixelGraph.optimizeNodes(new ProgressFeedback());

        }

        System.err.print(clearLine + "Rendering...");
        String svg = SvgRenderer.draw(pixelGraph, width, height);

        if(combine) {

            System.err.print(clearLine + "Combining paths...");
            svg = combinePaths(svg);

        }

        System.err.print(clearLine + "Writing SVG...");
        FileWriter writer = new FileWriter(outfile);
        writer.write(svg);
        writer.close();

        System.err.print(clearLine);
    }

}

