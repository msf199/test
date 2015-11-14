package main.com.whitespell.peak.logic.exec;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import main.com.whitespell.peak.Server;
import main.com.whitespell.peak.StaticRules;
import main.com.whitespell.peak.logic.EmailSend;
import main.com.whitespell.peak.logic.MandrillMailer;
import main.com.whitespell.peak.logic.RandomGenerator;
import main.com.whitespell.peak.logic.config.Config;
import main.com.whitespell.peak.logic.logging.Logging;
import main.com.whitespell.peak.logic.util.FileOps;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

/**
 * @author Pim de Witte(wwadewitte), Whitespell LLC
 *         11/20/14
 *         control
 */
public class ShellExecution {

    /**
     * Executes a command
     *
     * @param command          The exact command to execute
     * @return exit code integer representation
     */


    private static String cloudCommand = "bash createnode.sh $instance-id";
    private static String deleteCommand = "bash deletenode.sh $instance-id";

    public static int deleteNode(String instanceId) {
        String commandToRun = deleteCommand.replace("$instance-id", instanceId);
        return executeCommand(commandToRun);
    }

    public static void createAndInsertVideoConverter() {



        String instanceId = "vc-"+ Server.getMilliTime()/1000+"-"+ new Random().nextInt(100);
        String commandToRun = cloudCommand.replace("$instance-id", instanceId);

        // in avcpvm create function to retrieve instance based on internal IP and hostname command

          int status = executeCommand(commandToRun);

        String output = null;
        try {
            output = FileOps.readFile(instanceId + ".log", Charset.defaultCharset());
            Logging.log("CMD", output);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(output != null && output.contains("\n") && output.contains("RUNNING")) {
            System.out.println("output was: " + output);
            String[] lines = new String[2];
            // line 0 has the table names
            lines[0] = output.split("\n")[0].replaceAll(" +", " ");
            // line 1 has the node details
            lines[1] = output.split("\n")[1].replaceAll(" +", " ");

            /// NAME,ZONE, MACHINE_TYPE,PREEMPTIBLE,INTERNAL_IP,EXTERNAL_IP,STATUS
            String[] tableHeads = lines[0].split(" ");
            String[] nodeDetails = lines[1].split(" ");

            HashMap<String, String> instanceDetails = new HashMap<>();

            for(int i = 0; i < tableHeads.length; i++) {
                instanceDetails.put(tableHeads[i], nodeDetails[i]);
                System.out.println(tableHeads[i] + " : " + nodeDetails[i]);
            }


            try {
                HttpResponse stringResponse = Unirest.post("http://localhost:" + Config.API_PORT + "/videoprocessing/instances")
                        .header("accept", "application/json")
                        .header("X-Authentication", "-1," + StaticRules.MASTER_KEY)
                        .body("{\n" +
                                "\"instanceId\": \"" + instanceDetails.get("NAME") + "\",\n" +
                                "\"ipv4Address\": \"" + instanceDetails.get("EXTERNAL_IP") + "\"" +
                                "}")
                        .asString();
            } catch (UnirestException e) {
                e.printStackTrace();
            }

            MandrillMailer.sendDebugEmail(
                    "peak@whitepsell.com",
                    "Peak API",
                    "Created video node",
                    "With ip: "+instanceDetails.get("EXTERNAL_IP")+"",
                    "Details: The Peak API made a video node",
                    "Debug: (output)" + output,
                    "debug-email",
                    "pim@whitespell.com"
            );
        } else {
            Logging.log("HIGH", "Failed to create new video converter with debug message:" + output);
            //String fromEmail, String fromName, String subject, String name, String details, String debug, String templateName, String toEmail

                MandrillMailer.sendDebugEmail(
                        "peak@whitepsell.com",
                        "Peak API",
                        "Error in creating video nodes",
                        "Error in creating video nodes",
                        "Details: The Peak API failed to create a video processing node",
                        "Debug: (output)" + output,
                        "debug-email",
                        "pim@whitespell.com"
                );
        }


    }

    /**
     * Executes a command
     *
     * @param command          The exact command to execute
     * @return output of command
     */

    public static String returnOutputOfCommand(String command) {

        System.out.println(command);

        StringBuffer output = new StringBuffer();

        Process p = null;
        try {
            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line = "";
            while ((line = reader.readLine()) != null) {
                if(output.length() <= 0) {
                    output.append(line);
                } else {
                    output.append("\n" + line);
                }
            }
            reader.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return output.toString();

    }

    public static int executeCommand(String command) {
        System.out.println(command);

        StringBuffer output = new StringBuffer();

        Process p = null;
        try {
            p = Runtime.getRuntime().exec(command);
            p.waitFor();

                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(p.getInputStream()));

                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                    output.append(line + "\n");
                }
                reader.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return (p == null ? -1 : p.exitValue());

    }

    /**
     * Executes a shell script
     *
     * @param path to the shell script to execute
     * @return exit code integer representation
     */

    public static int readBashScript(String path, Object...params) {

        StringBuilder output = new StringBuilder();
        Process proc = null;

        StringBuilder parameters = new StringBuilder();
        for(int i = 0; i < params.length; i++) {
            parameters.append(" " + params[i]);
        }

        try {
            proc = Runtime.getRuntime().exec(path + parameters.toString()); //todo(pim) not reading these params, fix
            BufferedReader read = new BufferedReader(new InputStreamReader(
                    proc.getInputStream()));
            try {
                proc.waitFor();
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
            while (read.ready()) {
                output.append(read.readLine());
            }

            System.out.println(output);

            read.close();

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return (proc == null ? -1 : proc.exitValue());
    }

}
