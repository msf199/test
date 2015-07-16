package main.com.whitespell.peak.logic;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import main.com.whitespell.peak.StaticRules;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Pim de Witte(wwadewitte), Pim de Witte, Whitespell Inc., Whitespell LLC
 *         5/3/15
 *         whitespell.logic
 */
public class Safety {


    public static String getSalt() throws NoSuchAlgorithmException, NoSuchProviderException {
        //Always use a SecureRandom generator
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG", "SUN");
        //Create array for salt
        byte[] salt = new byte[16];
        //Get a random salt
        sr.nextBytes(salt);
        //return salt
        return salt.toString();
    }

    /**
     * Get a long as an integer and check that the integrity of the value isn't compromised.
     *
     * @param l The long value to convert to an integer.
     * @return the value of the long as an integer.
     */
    public static int safeLongToInt(long l) {
        if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(l + " cannot be cast to int without changing its value.");
        }
        return (int) l;
    }

    /**
     * Check whether or not a string is strictly numeric.
     *
     * @param string The string to check.
     * @return <i>true</i> if the string is strictly numeric.
     */
    public static boolean isInteger(String string) {
        try {
            int i = Integer.parseInt(string);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    /**
     * The checkPayload function checks the payload of a request for a number of things: 1) it checks all the keys in the required and optional parameters inputKeyTypeMap to see
     * if they are present, and if they are valid (e.g. integer is an actual integer). It is used to generate the JsonObject that is used in the request with these features built in to it.
     * @param inputKeyTypeMap   The map of keys that the endpoint would like to use, and the type of key it is. E.g. "category_id" is a REG_INT_REQUIRED (or required regular integer),
     *                          which will in this function be checked for presence, and integer validity.
     * @param rawPayload
     * @return
     * @throws InputNotValidException
     */
    public static void checkPayload(HashMap<String, StaticRules.InputTypes> inputKeyTypeMap, JsonElement rawPayload) throws InputNotValidException {

        JsonObject payload = null;

        try {
            payload = rawPayload.getAsJsonObject();
        } catch (Exception e) {
            throw new InputNotValidException("We were not able to parse the payload as a JSON object");
        }

        for (String key : inputKeyTypeMap.keySet()) {

            /**
             * Check whether key is found
             */

            if (payload.get(key) == null) {
                if(inputKeyTypeMap.get(key).isRequired()) {
                    throw new InputNotValidException("Required key " + key + " was not found in the payload.");
                } else {
                    continue;
                }
            }
            /**
             * Check against integer value
             */

            if (inputKeyTypeMap.get(key) == StaticRules.InputTypes.REG_INT_REQUIRED && !Safety.isInteger(payload.get(key).getAsString())
                    || inputKeyTypeMap.get(key) == StaticRules.InputTypes.REG_INT_OPTIONAL && !Safety.isInteger(payload.get(key).getAsString())
                    ) {
                throw new InputNotValidException("Value '"+key+"' was not a valid integer value.");
            }


            /** Check against values **/


            if (inputKeyTypeMap.get(key).getType().equals("int")) {
                if (payload.get(key).getAsInt() < inputKeyTypeMap.get(key).getMinLength()) {
                    throw new InputNotValidException("Integer key '"+key+"' was smaller than the minimum value.");// int is smaller than least allowed value
                } else if (payload.get(key).getAsInt() > inputKeyTypeMap.get(key).getMaxLength()) {
                    throw new InputNotValidException("Integer key '"+key+"' was not bigger than the maximum value.");
                }
            }

            /** Check string against max length **/

            if (inputKeyTypeMap.get(key).getType().equals("string")) {
                if (payload.get(key).getAsString().length() < inputKeyTypeMap.get(key).getMinLength()) {
                    throw new InputNotValidException("Required key '"+key+"' length was smaller than the minimum");
                } else if (payload.get(key).getAsString().length() > inputKeyTypeMap.get(key).getMaxLength()) {
                    throw new InputNotValidException("Required key '"+key+"' length was bigger than the maximum");
                }
            }

            if (inputKeyTypeMap.get(key).getType().equals("jsonarray")) {

            }
        }
    }

    /**
     * The checkPayload function checks the payload of a request for a number of things: 1) it checks all the keys in the required and optional parameters inputKeyTypeMap to see
     * if they are present, and if they are valid (e.g. integer is an actual integer). It is used to generate the JsonObject that is used in the request with these features built in to it.
     * @param inputKeyTypeMap   The map of keys that the endpoint would like to use, and the type of key it is. E.g. "category_id" is a REG_INT_REQUIRED (or required regular integer),
     *                          which will in this function be checked for presence, and integer validity.
     * @param parameterMap
     * @return
     * @throws InputNotValidException
     */
    public static void checkQueryStringInput(HashMap<String, StaticRules.InputTypes> inputKeyTypeMap, Map<String, String[]> parameterMap) throws InputNotValidException {

        for (String key : inputKeyTypeMap.keySet()) {

            /**
             * Check whether key is found
             */

            if (parameterMap.get(key) == null) {
                if(inputKeyTypeMap.get(key).isRequired()) {
                    throw new InputNotValidException("Required key '" + key + "' was not found in the query string. See the documentation for all the requirements.");
                } else {
                    continue;
                }
            }

            /** Check against values **/


            if (inputKeyTypeMap.get(key).getType().equals("int")) {

                /**
                 * Check against valid integer value
                 */

            System.out.println(key + " ->" + Arrays.toString(parameterMap.get(key)));
                if (inputKeyTypeMap.get(key) == StaticRules.InputTypes.REG_INT_REQUIRED
                        && !Safety.isInteger(parameterMap.get(key)[0])
                        || inputKeyTypeMap.get(key) == StaticRules.InputTypes.REG_INT_OPTIONAL && !Safety.isInteger(parameterMap.get(key)[0])
                        ) {
                    throw new InputNotValidException("Value '"+key+"' was not a valid integer value.");
                }


                if (Integer.parseInt(parameterMap.get(key)[0]) < inputKeyTypeMap.get(key).getMinLength()) {
                    throw new InputNotValidException("Integer key '"+key+"' was smaller than the minimum value.");// int is smaller than least allowed value
                } else if (Integer.parseInt(parameterMap.get(key)[0]) > inputKeyTypeMap.get(key).getMaxLength()) {
                    throw new InputNotValidException("Integer key '"+key+"' was not bigger than the maximum value.");
                }
            }

            /** Check string against max length **/

            if (inputKeyTypeMap.get(key).getType().equals("string")) {
                if (parameterMap.get(key)[0].length() < inputKeyTypeMap.get(key).getMinLength()) {
                    throw new InputNotValidException("Required key '"+key+"' length was smaller than the minimum");
                } else if (parameterMap.get(key)[0].length() > inputKeyTypeMap.get(key).getMaxLength()) {
                    throw new InputNotValidException("Required key '"+key+"' length was bigger than the maximum");
                }
            }
        }
    }

    /**
     * The checkPayload function checks the payload of a request for a number of things: 1) it checks all the keys in the required and optional parameters inputKeyTypeMap to see
     * if they are present, and if they are valid (e.g. integer is an actual integer). It is used to generate the JsonObject that is used in the request with these features built in to it.
     * @param inputKeyTypeMap   The map of keys that the endpoint would like to use, and the type of key it is. E.g. "category_id" is a REG_INT_REQUIRED (or required regular integer),
     *                          which will in this function be checked for presence, and integer validity.
     * @param parameterMap
     * @return
     * @throws InputNotValidException
     */
    public static void checkUrlVariableInput(HashMap<String, StaticRules.InputTypes> inputKeyTypeMap, Map<String, String> parameterMap) throws InputNotValidException {

        for (String key : inputKeyTypeMap.keySet()) {

            /**
             * Check whether key is found
             */

            if (parameterMap.get(key) == null) {
                if(inputKeyTypeMap.get(key).isRequired()) {
                    throw new InputNotValidException("Unique required key " + key + " was not found in the URL variables.");
                } else {
                    continue;
                }
            }

            /** Check against values **/


            if (inputKeyTypeMap.get(key).getType().equals("int")) {

                /**
                 * Check against valid integer value
                 */

                if (inputKeyTypeMap.get(key) == StaticRules.InputTypes.REG_INT_REQUIRED && !Safety.isInteger(parameterMap.get(key))
                        || inputKeyTypeMap.get(key) == StaticRules.InputTypes.REG_INT_OPTIONAL && !Safety.isInteger(parameterMap.get(key))
                        ) {
                    throw new InputNotValidException("Value '"+key+"' was not a valid integer value.");
                }


                if (Integer.parseInt(parameterMap.get(key)) < inputKeyTypeMap.get(key).getMinLength()) {
                    throw new InputNotValidException("Integer key '"+key+"' was smaller than the minimum value.");// int is smaller than least allowed value
                } else if (Integer.parseInt(parameterMap.get(key)) > inputKeyTypeMap.get(key).getMaxLength()) {
                    throw new InputNotValidException("Integer key '"+key+"' was not bigger than the maximum value.");
                }
            }

            /** Check string against max length **/

            if (inputKeyTypeMap.get(key).getType().equals("string")) {
                if (parameterMap.get(key).length() < inputKeyTypeMap.get(key).getMinLength()) {
                    throw new InputNotValidException("Required key '"+key+"' length was smaller than the minimum");
                } else if (parameterMap.get(key).length() > inputKeyTypeMap.get(key).getMaxLength()) {
                    throw new InputNotValidException("Required key '"+key+"' length was bigger than the maximum");
                }
            }
        }
    }


}
