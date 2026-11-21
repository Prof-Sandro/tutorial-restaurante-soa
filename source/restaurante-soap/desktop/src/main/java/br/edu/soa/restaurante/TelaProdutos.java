// Finalidade: Montar a janela administrativa com os painéis de cadastro.
package br.edu.soa.restaurante;

import br.edu.soa.restaurante.soap.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public final class TelaProdutos extends Janela {
	public TelaProdutos(ConexaoSoap conexao) {
		super("Restaurante • Administração", conexao);
		JTabbedPane abas = new JTabbedPane();
		PainelMesas mesas = new PainelMesas(this);
		PainelCategorias categorias = new PainelCategorias(this);
		PainelProdutos produtos = new PainelProdutos(this);
		abas.addTab("Mesas", mesas);
		abas.addTab("Categorias", categorias);
		abas.addTab("Produtos", produtos);
		add(abas, BorderLayout.CENTER);
		abas.addChangeListener(e -> {
			if (abas.getSelectedComponent() == produtos)
				produtos.categorias();
			else if (abas.getSelectedComponent() == mesas)
				mesas.carregar();
			else
				categorias.carregar();
		});
		mesas.carregar();
	}

	// Inicia a aplicação correspondente a esta classe.
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			var c = login("admin");
			if (c != null)
				new TelaProdutos(c).setVisible(true);
		});
	}
}
