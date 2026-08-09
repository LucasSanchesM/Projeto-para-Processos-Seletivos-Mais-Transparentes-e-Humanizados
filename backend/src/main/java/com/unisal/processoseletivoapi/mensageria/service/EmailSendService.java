package com.unisal.processoseletivoapi.mensageria.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.unisal.processoseletivoapi.mensageria.EmailNotifier;
import com.unisal.processoseletivoapi.mensageria.dto.EmailSendRequest;

@Service
public class EmailSendService implements EmailNotifier {
    @Value("${resend.api.key}")
    private String apiKey;

    @Override
    public void enviarConfirmacaoEmail(EmailSendRequest request) {
        Resend resend = new Resend(apiKey);

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

                    <table
                        width="100%%"
                        cellpadding="0"
                        cellspacing="0"
                        border="0"
                        style="padding: 40px 16px;"
                    >
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

                                            <!-- Logo / Nome -->
                                            <p style="
                                                margin: 0 0 32px 0;
                                                color: #222222;
                                                font-size: 15px;
                                                font-weight: 500;
                                            ">
                                                Plataforma
                                            </p>

                                            <!-- Título -->
                                            <h1 style="
                                                margin: 0 0 12px 0;
                                                color: #4A4A4A;
                                                font-size: 19px;
                                                line-height: 28px;
                                                font-weight: 500;
                                            ">
                                                Confirme seu endereço de e-mail
                                            </h1>

                                            <!-- Descrição -->
                                            <p style="
                                                margin: 0 0 32px 0;
                                                color: #686868;
                                                font-size: 15px;
                                                line-height: 24px;
                                                font-weight: 400;
                                            ">
                                                Para concluir seu cadastro, utilize o código
                                                de verificação abaixo.
                                            </p>


                                            <!-- Código -->
                                            <div style="
                                                background-color: #F7F7F7;
                                                border: 1px solid #E6DECC;
                                                border-radius: 12px;
                                                padding: 24px;
                                                text-align: center;
                                                margin-bottom: 32px;
                                            ">

                                                <p style="
                                                    margin: 0 0 8px 0;
                                                    color: #919191;
                                                    font-size: 13px;
                                                    line-height: 20px;
                                                ">
                                                    Seu código de verificação
                                                </p>

                                                <p style="
                                                    margin: 0;
                                                    color: #222222;
                                                    font-size: 32px;
                                                    line-height: 40px;
                                                    font-weight: 500;
                                                ">
                                                    %s
                                                </p>

                                            </div>


                                            <!-- Expiração -->
                                            <p style="
                                                margin: 0 0 8px 0;
                                                color: #686868;
                                                font-size: 14px;
                                                line-height: 22px;
                                            ">
                                                Este código é válido por <strong style="color:#494949;">10 minutos</strong>.
                                            </p>

                                            <p style="
                                                margin: 0;
                                                color: #919191;
                                                font-size: 14px;
                                                line-height: 22px;
                                            ">
                                                Se você não solicitou esta verificação,
                                                nenhuma ação é necessária.
                                            </p>

                                        </td>
                                    </tr>

                                    <!-- Footer -->
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
                .formatted(request.code());

        CreateEmailOptions sendEmailRequest = CreateEmailOptions.builder()
                .from("Plataforma <noreply@gabrieldiassantiago.dev>")
                .to(request.email())
                .subject("Código de Verificação - Processo Seletivo")
                .html(htmlContent)
                .build();

        try {
            resend.emails().send(sendEmailRequest);
        } catch (ResendException e) {
            e.printStackTrace();
        }
    }
}