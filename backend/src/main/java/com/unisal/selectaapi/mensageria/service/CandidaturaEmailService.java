package com.unisal.selectaapi.mensageria.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.unisal.selectaapi.mensageria.CandidaturaNotifier;
import com.unisal.selectaapi.mensageria.dto.CandidaturaEmailRequest;

@Service
public class CandidaturaEmailService implements CandidaturaNotifier {

    @Value("${resend.api.key}")
    private String apiKey;

    @Override
    public void enviarAtualizacaoCandidatura(CandidaturaEmailRequest request) {
        Resend resend = new Resend(apiKey);

        String empresa = request.empresaNome() != null ? request.empresaNome() : "A empresa";
        String etapa = request.etapaDescricao() != null ? request.etapaDescricao() : "-";
        String status = request.statusDescricao() != null ? request.statusDescricao() : "-";
        String mensagem = request.mensagem() != null && !request.mensagem().isBlank()
                ? request.mensagem()
                : "Acesse a plataforma para ver os detalhes e os feedbacks desta etapa.";

        String htmlContent = """
                <!DOCTYPE html>
                <html lang="pt-BR">
                <head>
                    <meta charset="UTF-8">
                </head>

                <body style="
                    margin: 0;
                    padding: 0;
                    background-color: #f7f7f7;
                    font-family: Inter, Arial, sans-serif;
                ">

                    <table width="100%%" cellpadding="0" cellspacing="0" border="0" style="padding: 40px 16px;">
                        <tr>
                            <td align="center">

                                <table
                                    width="100%%"
                                    cellpadding="0"
                                    cellspacing="0"
                                    border="0"
                                    style="
                                        max-width: 520px;
                                        background-color: #FEFEFE;
                                        border: 1px solid #E6DECC;
                                        border-radius: 14px;
                                    "
                                >

                                    <tr>
                                        <td style="padding: 40px 40px 32px 40px;">

                                            <p style="
                                                margin: 0 0 32px 0;
                                                color: #222222;
                                                font-size: 15px;
                                                font-weight: 500;
                                            ">
                                                Plataforma
                                            </p>

                                            <h1 style="
                                                margin: 0 0 12px 0;
                                                color: #4A4A4A;
                                                font-size: 19px;
                                                line-height: 28px;
                                                font-weight: 500;
                                            ">
                                                Olá, %s. Sua candidatura foi atualizada
                                            </h1>

                                            <p style="
                                                margin: 0 0 32px 0;
                                                color: #686868;
                                                font-size: 15px;
                                                line-height: 24px;
                                            ">
                                                %s atualizou o seu processo seletivo para a vaga
                                                <strong style="color:#494949;">%s</strong>.
                                            </p>

                                            <div style="
                                                background-color: #F7F7F7;
                                                border: 1px solid #E6DECC;
                                                border-radius: 12px;
                                                padding: 24px;
                                                margin-bottom: 32px;
                                            ">

                                                <p style="
                                                    margin: 0 0 8px 0;
                                                    color: #919191;
                                                    font-size: 13px;
                                                    line-height: 20px;
                                                ">
                                                    Etapa atual
                                                </p>

                                                <p style="
                                                    margin: 0 0 20px 0;
                                                    color: #222222;
                                                    font-size: 20px;
                                                    line-height: 28px;
                                                    font-weight: 500;
                                                ">
                                                    %s
                                                </p>

                                                <p style="
                                                    margin: 0 0 8px 0;
                                                    color: #919191;
                                                    font-size: 13px;
                                                    line-height: 20px;
                                                ">
                                                    Situação
                                                </p>

                                                <p style="
                                                    margin: 0;
                                                    color: #222222;
                                                    font-size: 16px;
                                                    line-height: 24px;
                                                    font-weight: 500;
                                                ">
                                                    %s
                                                </p>

                                            </div>

                                            <p style="
                                                margin: 0;
                                                color: #686868;
                                                font-size: 14px;
                                                line-height: 22px;
                                            ">
                                                %s
                                            </p>

                                        </td>
                                    </tr>

                                    <tr>
                                        <td style="
                                            padding: 24px 40px;
                                            border-top: 1px solid #E6DECC;
                                        ">
                                            <p style="
                                                margin: 0;
                                                color: #919191;
                                                font-size: 12px;
                                                line-height: 18px;
                                            ">
                                                Este é um e-mail automático.
                                                Por favor, não responda esta mensagem.
                                            </p>
                                        </td>
                                    </tr>

                                </table>

                            </td>
                        </tr>
                    </table>

                </body>
                </html>
                """
                .formatted(
                        request.candidatoNome(),
                        empresa,
                        request.vagaTitulo(),
                        etapa,
                        status,
                        mensagem);

        CreateEmailOptions sendEmailRequest = CreateEmailOptions.builder()
                .from("Plataforma <noreply@gabrieldiassantiago.dev>")
                .to(request.email())
                .subject("Atualização da sua candidatura - " + request.vagaTitulo())
                .html(htmlContent)
                .build();

        try {
            resend.emails().send(sendEmailRequest);
        } catch (ResendException e) {
            e.printStackTrace();
        }
    }
}
