package br.com.alura.desafio;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.HashMap;
import java.util.Map;

public class ConversorDeMoedas {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        Gson gson = new Gson();

        // 1. Obter as taxas de conversão da API
        String jsonResponse = obterTaxasDaApi("USD"); // Base da conversão (ex: Dólar)
        JsonObject jsonObject = gson.fromJson(jsonResponse, JsonObject.class);

        // 2. Criar um menu com as opções de moedas
        Map<String, String> moedas = new HashMap<>();
        moedas.put("BRL", "Real");
        moedas.put("EUR", "Euro");
        moedas.put("JPY", "Yen");
        moedas.put("USD", "Dolar");

        System.out.println("Escolha a moeda de origem (BRL, EUR, JPY, USD):");
        String moedaOrigem = scanner.nextLine().toUpperCase(); // Garante que esteja em maiúsculas

        System.out.println("Escolha a moeda de destino (BRL, EUR, JPY, USD):");
        String moedaDestino = scanner.nextLine().toUpperCase(); // Garante que esteja em maiúsculas


        // 3. Solicitar o valor a ser convertido
        System.out.print("Digite o valor a ser convertido: ");
        double valor = scanner.nextDouble();

        // 4. Realizar a conversão e exibir o resultado
        double valorConvertido = converterMoeda(jsonObject, moedaOrigem, moedaDestino, valor);
        System.out.println("O valor convertido é: " + valorConvertido);
    }

    // Método para obter as taxas da API
    private static String obterTaxasDaApi(String base) throws Exception {
        try {
            String apiUrl = "https://v6.exchangerate-api.com/v6/41a64fb23984d87d0dff463b/latest/USD";
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));

                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                return response.toString();
            } else {
                throw new
                        RuntimeException("Falha ao obter as taxas de conversão: " + responseCode);
            }

        } catch (ConnectException e) {
            System.err.println("Erro de conexão com a API: " + e.getMessage());
            System.err.println("Verifique sua conexão com a internet e tente novamente mais tarde.");
        }
        return base;
    }

    // Método para converter a moeda
    private static double converterMoeda(JsonObject jsonObject, String moedaOrigem, String moedaDestino, double valor) {
        JsonObject rates;

        // Tenta obter o objeto correto
        if (jsonObject.has("rates")) {
            rates = jsonObject.getAsJsonObject("rates");
        } else if (jsonObject.has("conversion_rates")) {
            rates = jsonObject.getAsJsonObject("conversion_rates");
        } else {
            throw new RuntimeException("Objeto 'rates' ou 'conversion_rates' não encontrado na resposta da API.");
        }

        // Verificar se as moedas existem no JSON
        if (!rates.has(moedaOrigem) || !rates.has(moedaDestino)) {
            throw new RuntimeException("Moeda de origem ou destino não encontrada.");
        }

        // Obter as taxas de câmbio
        double taxaOrigem = rates.get(moedaOrigem).getAsDouble();
        double taxaDestino = rates.get(moedaDestino).getAsDouble();
        return valor * (taxaDestino / taxaOrigem);
    }

}