import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main {
    private static final String TOKEN = "127003503:AAEtO42j0Kca3qdNT3ltlvnm6g16uR-r1yA";
    private static final String BASEURL = "https://api.telegram.org/bot";

//    private static final String[] gadgets = {
//            "Профессор что, сам должен для вас двери открывать?",
//            "Екатерина, как вас зовут?",
//            "Вам это не поможет!",
//            "Вы сколько пар пропустили?",
//            "Меня не интересует, почему вас не было.",
//            "Ви щито, пьяная?",
//            "Катерина, вы знаете этого молодого человека?",
//            "Ви щито, больной?",
//            "Вы зачем отравились?",
//    };

    private static final String[] questions = {
            "q1", "q2", "q3", "q4", "q5"
    };

    private static int offset;
    private static Random rnd;

    /**
     * Работа программы начинается здесь.
     *
     * @param args - аргументы командной строки (нам не нужны).
     */
    public static void main(String[] args) {
        System.out.println("Вас приветствует гаджебот для не-курятника!");
        rnd = new Random();
        readMessages();
    }

    /**
     * Читаем и анализируем пришедшие нам сообщения в бесконечном цикле.
     */
    private static void readMessages() {
        while (true) {
            CloseableHttpClient httpclient = HttpClientBuilder.create().build();
            String requestUrl = BASEURL + TOKEN + "/" + "getUpdates";
            HttpPost httpPost = new HttpPost(requestUrl);
            try {
                List<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("offset", Integer.toString(offset)));
                httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
                HttpResponse response;
                response = httpclient.execute(httpPost);
                HttpEntity entity = response.getEntity();
                BufferedHttpEntity buf = new BufferedHttpEntity(entity);
                String responseContent = EntityUtils.toString(buf, "UTF-8");
                JSONObject jsonObject = new JSONObject(responseContent);
                if (!jsonObject.getBoolean("ok")) {
                    System.out.println("Ошибка в принятых данных");
                    continue;
                }
                JSONArray jsonArray = jsonObject.getJSONArray("result");
                if (jsonArray.length() != 0) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject o = jsonArray.getJSONObject(i);
                        System.out.println(o.toString());
                        int newUpdateId = o.getInt("update_id");
                        if (newUpdateId >= offset)
                            offset = newUpdateId + 1;
                        JSONObject message = o.getJSONObject("message");
                        String text = "";
                        try {
                            text = message.getString("text");
                        }
                        catch (JSONException e) {
                            System.out.println(e.toString());
                            continue;
                        }
                        JSONObject chat = message.getJSONObject("chat");
                        int chatId = chat.getInt("id");
                        processGadge(text, chatId);
                    }
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    System.out.println(e.toString());
                }
            } catch (IOException e) {
                System.out.println(e.toString());
            }
        }
    }

    /**
     * Проверяем, есть ли в сообщении слово "гадж" и, если есть, отпраляем гадже-фразу.
     *
     * @param message - сообщение для анализа.
     * @param chatId  - id чата или пользователя, от которого получено сообщение.
     */
    private static void processGadge(String message, int chatId) {
        message = message.toLowerCase().trim();
        if (message.contains("гадж")) {
            System.out.println("Отправляем гадже-ответ");
            sendText(gadgets[rnd.nextInt(gadgets.length - 1)], chatId);
        }
    }

    /**
     * Отправляем сообщение.
     *
     * @param text   - текст сообщения для отправки.
     * @param chatId - id пользователя или группы, куда отправлять сообщение.
     */
    private static void sendText(String text, int chatId) {
        CloseableHttpClient httpclient = HttpClientBuilder.create().build();
        String requestUrl = BASEURL + TOKEN + "/" + "sendMessage";
        HttpPost httpPost = new HttpPost(requestUrl);
        try {
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("chat_id", Integer.toString(chatId)));
            params.add(new BasicNameValuePair("text", text));
            httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            httpclient.execute(httpPost);
        } catch (IOException e) {
            System.out.println(e.toString());
        }
    }
}