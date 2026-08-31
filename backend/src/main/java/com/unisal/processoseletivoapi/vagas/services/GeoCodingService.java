package com.unisal.processoseletivoapi.vagas.services;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.unisal.processoseletivoapi.exception.BusinessException;
import com.unisal.processoseletivoapi.vagas.dto.EnderecoDto;

import tools.jackson.databind.JsonNode;

@Service
public class GeoCodingService {

    private final RestClient restClient;

    public GeoCodingService() {
        this.restClient = RestClient.builder()
                            .baseUrl("https://nominatim.openstreetmap.org")
                            .defaultHeader("User-Agent", "processoseletivoapi/teste")
                            .build();
    }
    
    public EnderecoDto retornarEndereco(Double latitude, Double longitude){
        //Aqui ele pega a url base que esta em rest client adiciona os parametros para a requisição,
        //Como o retorno do nomination pode variar a gente usa JsonNode pra pegar os valores
        JsonNode response = restClient.get().uri(uriBuilder -> uriBuilder
                                                            .path("/reverse")
                                                            .queryParam("lat", latitude)
                                                            .queryParam("lon", longitude)
                                                            .queryParam("format", "json")
                                                            .queryParam("zoom", 10)
                                                            .build()).retrieve().body(JsonNode.class);

        //pega todas as infos que estão em endereço para tratar
        JsonNode adress = response.get("address");
        if(adress == null){
            throw new BusinessException(HttpStatus.UNPROCESSABLE_ENTITY, "localização invalida");
        }

        return new EnderecoDto(coletarCidade(adress), coletarEstado(adress));
    }
    

    public String coletarCidade(JsonNode adress){
        if(adress.has("city")){
            return adress.get("city").asText();
        }
        else if(adress.has("town")){
            return adress.get("town").asText();
        }
        else if(adress.has("village")){
            return adress.get("village").asText();
        }
        else if(adress.has("municipality")){
            return adress.get("municipality").asText();
        }
        else{
            throw new BusinessException(HttpStatus.UNPROCESSABLE_ENTITY, "localização invalida");
        }
    }

    public String coletarEstado(JsonNode adress){
        if(adress.has("state")){
            return siglaEstado(adress.get("state").asText());
        }
        else{
            throw new BusinessException(HttpStatus.UNPROCESSABLE_ENTITY, "localização invalida");
        }
    }

    private String siglaEstado(String nomeEstado) {
    //Pegar sigla do estado
    return switch (nomeEstado) {
        case "Acre" -> "AC";
        case "Alagoas" -> "AL";
        case "Amapá" -> "AP";
        case "Amazonas" -> "AM";
        case "Bahia" -> "BA";
        case "Ceará" -> "CE";
        case "Distrito Federal" -> "DF";
        case "Espírito Santo" -> "ES";
        case "Goiás" -> "GO";
        case "Maranhão" -> "MA";
        case "Mato Grosso" -> "MT";
        case "Mato Grosso do Sul" -> "MS";
        case "Minas Gerais" -> "MG";
        case "Pará" -> "PA";
        case "Paraíba" -> "PB";
        case "Paraná" -> "PR";
        case "Pernambuco" -> "PE";
        case "Piauí" -> "PI";
        case "Rio de Janeiro" -> "RJ";
        case "Rio Grande do Norte" -> "RN";
        case "Rio Grande do Sul" -> "RS";
        case "Rondônia" -> "RO";
        case "Roraima" -> "RR";
        case "Santa Catarina" -> "SC";
        case "São Paulo" -> "SP";
        case "Sergipe" -> "SE";
        case "Tocantins" -> "TO";
        default -> throw new BusinessException(HttpStatus.UNPROCESSABLE_ENTITY, "estado não reconhecido: " + nomeEstado);
    };
}
}
