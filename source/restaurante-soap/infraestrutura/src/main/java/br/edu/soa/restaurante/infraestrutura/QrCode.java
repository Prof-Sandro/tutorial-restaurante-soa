// Finalidade: Produzir a imagem PNG do QR com a URL pública do cardápio.
package br.edu.soa.restaurante.infraestrutura;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import com.google.zxing.*;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;

import br.edu.soa.restaurante.aplicacao.Falha;

public final class QrCode {
	
	private QrCode() {	}

	// Valida a base e a mesa e produz os bytes da imagem PNG.
	public static byte[] gerar(String base, int mesa) {
		if (mesa < 1 || mesa > 9999)
			throw Falha.invalido("Número da mesa entre 1 e 9999.");
		if (base == null || base.isBlank())
			throw new Falha("CONFIGURACAO", Falha.Tipo.INTERNO,
					"Configure PUBLIC_BASE_URL com a URL acessível aos clientes.");
		try {
			URI u = URI.create(base);
			if (!java.util.Set.of("http", "https").contains(u.getScheme()) || u.getHost() == null || u.getUserInfo() != null || u.getQuery() != null || u.getFragment() != null)
				throw new IllegalArgumentException();
			String url = base.replaceAll("/+$", "") + "/cardapio/index.html?mesa=" + mesa;
			
			var matriz = new QRCodeWriter().encode(url, BarcodeFormat.QR_CODE, 320, 320);
			
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			
			MatrixToImageWriter.writeToStream(matriz, "PNG", out);
			return out.toByteArray();
		} catch (Exception e) {
			throw new Falha("CONFIGURACAO", Falha.Tipo.INTERNO,
					"Não foi possível gerar o QR Code. Confira PUBLIC_BASE_URL.");
		}
	}
}
