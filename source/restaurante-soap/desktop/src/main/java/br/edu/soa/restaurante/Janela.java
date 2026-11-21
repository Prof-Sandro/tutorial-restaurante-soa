// Finalidade: Separar chamadas remotas do trabalho de apresentação na EDT.
package br.edu.soa.restaurante;

import br.edu.soa.restaurante.soap.ErroNegocio_Exception;
import javax.swing.*;
import java.awt.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

/** doInBackground faz rede; done roda na EDT. Só uma operação por janela. */
abstract class Janela extends JFrame {
	protected final ConexaoSoap conexao;
	private boolean ocupado;
	private final JLabel mensagem = new JLabel("Pronto");

	protected Janela(String titulo, ConexaoSoap conexao) {
		super(titulo);
		this.conexao = conexao;
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setSize(1150, 760);
		setLocationRelativeTo(null);
		add(mensagem, BorderLayout.SOUTH);
	}

	// Executa o trabalho remoto fora da EDT e entrega a atualização à interface.
	protected <T> void remoto(Callable<T> acao, Consumer<T> sucesso) {
		if (ocupado)
			return;
		ocupado = true;
		habilitar(getContentPane(), false);
		mensagem.setText("Comunicando com o servidor…");
		new SwingWorker<T, Void>() {
			@Override
			protected T doInBackground() throws Exception {
				return acao.call();
			}

			@Override
			protected void done() {
				ocupado = false;
				habilitar(getContentPane(), true);
				mensagem.setText("Pronto");
				try {
					sucesso.accept(get());
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				} catch (ExecutionException e) {
					erro(e.getCause());
				} catch (RuntimeException e) {
					erro(e);
				}
				if (!ocupado)
					atualizarEstado();
			}
		}.execute();
	}

	// Ajusta os controles ao estado recebido do pedido.
	protected void atualizarEstado() {
	}

	protected void erro(Throwable erro) {
		String texto = erro instanceof ErroNegocio_Exception f ? f.getFaultInfo().getMensagem()
				: erro instanceof IllegalArgumentException ? erro.getMessage()
						: "Não foi possível concluir. Verifique conexão, credenciais, servidor e tente atualizar.";
		JOptionPane.showMessageDialog(this, texto, "Restaurante", JOptionPane.ERROR_MESSAGE);
	}

	private static void habilitar(Container c, boolean v) {
		for (Component x : c.getComponents()) {
			x.setEnabled(v);
			if (x instanceof Container filho)
				habilitar(filho, v);
		}
	}

	// Obtém as credenciais da interface para criar a conexão autenticada.
	protected static ConexaoSoap login(String usuario) {
		JTextField nome = new JTextField(usuario);
		JPasswordField senha = new JPasswordField();
		JPanel campos = new JPanel(new GridLayout(0, 1, 4, 4));
		campos.add(new JLabel("Usuário"));
		campos.add(nome);
		campos.add(new JLabel("Senha"));
		campos.add(senha);
		if (JOptionPane.showConfirmDialog(null, campos, "Entrar no restaurante",
				JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION)
			return null;
		char[] valor = senha.getPassword();
		try {
			return new ConexaoSoap(nome.getText().trim(), new String(valor));
		} finally {
			java.util.Arrays.fill(valor, '\0');
			senha.setText("");
		}
	}
}