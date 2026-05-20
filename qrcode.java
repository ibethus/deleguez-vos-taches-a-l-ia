//usr/bin/env jbang "$0" "$@" ; exit $?
//
//DEPS uk.org.okapibarcode:okapibarcode:0.5.2
//DEPS info.picocli:picocli:4.5.0

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.concurrent.Callable;

import picocli.CommandLine.Command;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import uk.org.okapibarcode.backend.QrCode;
import uk.org.okapibarcode.graphics.Color;
import uk.org.okapibarcode.output.SvgRenderer;

@Command(name = "qrcode", mixinStandardHelpOptions = true, version = "qrcode 0.1", description = "Make a QR code with an overlay image. Inspired by https://hollycummins.com/creating-QR-codes/")
class main implements Callable<Integer> {

    @Parameters(index = "0", description = "Text to encode")
    String value;

    @Option(names = {"-l", "--length"}, description = "Image height", required = true, defaultValue = "200")
    int length;

    @Option(names = {"-o", "--output"}, description = "Output file", defaultValue = "qrcode.svg")
    Path outPath;

    @Option(names = {"-b", "--background"}, description = "Background color", required = true, defaultValue = "FFFFFF")
    String background;

    @Option(names = {"-f", "--foreground"}, description = "Foreground color", required = true, defaultValue = "000000")
    String foreground;


    public static void main(String[] args) throws Exception {
        new picocli.CommandLine(new main()).execute(args);
    }

    public Integer call() {
        encode(value, outPath, foreground, background, length);
        if (outPath.toFile().exists()) {
            System.out.println("Created QR code at " + outPath);
            System.exit(ExitCode.OK); // hard exit to avoid OSX AWT delay
        } else {
            System.out.println("Could not create QR code at " + outPath);
        }
        return ExitCode.OK;
    }

    public void encode(String value, Path outPath, String foreground, String background, int length) {
        QrCode qrCode = new QrCode();
        qrCode.setContent(value);
        qrCode.setPreferredEccLevel(QrCode.EccLevel.L); // Low error correction provides smallest QR code size

        // Strip leading # from hex color codes if present
        if (foreground.startsWith("#")) {
            foreground = foreground.substring(1);
        }
        if (background.startsWith("#")) {
            background = background.substring(1);
        }

        // Generate SVG representation of the QR code
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        SvgRenderer renderer = new SvgRenderer(out, 1.0, new Color(background), new Color(foreground), true);
        try {
            renderer.render(qrCode);
            Files.write(outPath, out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Error rendering QR code", e);
        }

    }
}