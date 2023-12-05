Apresentação do código

Utilizei o android Studio para fazer o programa, com ajuda de foruns, pesquisas no google e ferramentas de IA para aprendizado.

Como não tinha um projeto para melhorar da matéria PIT I.

Acabei criando o código do zero.

Segue o código e uma pequena explicação.


// Pacote e imports necessários para o projeto Android

package com.example.campominado;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;

 
// Declaração da Classe]

public class CampoMinadoActivity extends AppCompatActivity {

    // Constantes definindo o tamanho do campo e o número de bombas
    private static final int SIZE = 7;
    private static final int MINES = 8;

    // Arrays para armazenar informações sobre os botões, bombas, etc.
    private Button[][] buttons;
    private boolean[][] mines;
    private boolean[][] revealed;
    private boolean[][] flagged;

    // Handler para lidar com eventos assíncronos
    private Handler handler;

    // Método chamado na criação da atividade
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_campo_minado);

        // Inicialização do handler
        handler = new Handler();

        // Inicialização dos arrays
        buttons = new Button[SIZE][SIZE];
        mines = new boolean[SIZE][SIZE];
        revealed = new boolean[SIZE][SIZE];
        flagged = new boolean[SIZE][SIZE];

        // Configuração do GridLayout na interface
        GridLayout gridLayout = findViewById(R.id.gridLayout);
        configureGridLayout();

        // Configuração do botão "Novo Jogo"
        Button novoJogoButton = findViewById(R.id.btnNovoJogo);
        novoJogoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetGame();
            }
        });
    }

// Inicialização do Jogo

    // Método para inicializar o jogo com bombas aleatórias
    private void initializeGame() {
        Random random = new Random();
        int minesToPlace = MINES;
        while (minesToPlace > 0) {
            int row = random.nextInt(SIZE);
            int col = random.nextInt(SIZE);
            if (!mines[row][col]) {
                mines[row][col] = true;
                minesToPlace--;
            }
        }
    }

// Configuração do Layout 

    // Método para configurar o GridLayout com base nas dimensões da tela
    private void configureGridLayout() {
        // Obter dimensões da tela
        Display display = getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        int height = display.getHeight();

        // Configurar o tamanho do GridLayout com base nas dimensões da tela
        int gridSize = Math.min(width, height) * 2 / 3;
        GridLayout gridLayout = findViewById(R.id.gridLayout);
        gridLayout.getLayoutParams().width = gridSize;
        gridLayout.getLayoutParams().height = gridSize;
        gridLayout.setColumnCount(SIZE);

        // Inicializar o jogo e configurar os botões no GridLayout
        initializeGame();
        setupGridLayout();
    }
    
Configuração dos Botões no Layout

    // Método para configurar os botões no GridLayout
    private void setupGridLayout() {
        GridLayout gridLayout = findViewById(R.id.gridLayout);
        gridLayout.removeAllViews();

        int gridSize = gridLayout.getLayoutParams().width;
        int buttonSize = gridSize / SIZE;

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                Button button = new Button(this);
                // Configuração de eventos de toque e clique nos botões
                configureButton(button, row, col);
                buttons[row][col] = button;
                // Configuração dos parâmetros do botão no GridLayout
                configureButtonLayout(button, buttonSize, row, col);
                // Adição do botão ao GridLayout
                gridLayout.addView(button);
            }
        }
    }

Configuração de Eventos Toque/Pressionar

    // Método para configurar eventos de toque e clique nos botões
    private void configureButton(Button button, int row, int col) {
        button.setLayoutParams(new GridLayout.LayoutParams());
        button.setTag(new int[]{row, col});
        button.setOnTouchListener(new View.OnTouchListener() {
            // Lógica para clique longo
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int action = motionEvent.getAction();
                int[] position = (int[]) view.getTag();
                int row = position[0];
                int col = position[1];

                if (action == MotionEvent.ACTION_DOWN) {
                    // Início do clique longo
                    longClickHandler(row, col);
                } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                    // Fim do clique longo
                    handler.removeCallbacksAndMessages(null);
                    if (SystemClock.uptimeMillis() - initialClickTime < LONG_CLICK_DURATION) {
                        // Clique curto
                        onCellClick(row, col);
                    }
                }
                return true;
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            // Lógica para clique curto
            @Override
            public void onClick(View view) {
                handler.removeCallbacksAndMessages(null);
                int[] position = (int[]) view.getTag();
                onCellClick(position[0], position[1]);
            }
        });
    }

// Tratamento de Cliques e Métodos Auxiliares

    // Variáveis para lidar com cliques longos
    private long initialClickTime;
    private final long LONG_CLICK_DURATION = 1000;

    // Lógica para clique longo
    private void longClickHandler(int row, int col) {
        initialClickTime = SystemClock.uptimeMillis();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                onCellLongClick(row, col);
            }
        }, LONG_CLICK_DURATION);
    }

    // Método chamado ao clicar em uma célula
    private void onCellClick(int row, int col) {
        if (mines[row][col]) {
            // Se clicar em uma bomba, revela todas as bombas e mostra "Game Over!"
            revealMines();
            showToast("Game Over!");
        } else {
            // Se clicar em uma célula sem bomba, revela a célula e verifica se o jogador venceu
            revealCell(row, col);
            checkGameWin();
        }
    }

    // Método chamado ao manter pressionada uma célula
    private void onCellLongClick(int row, int col) {
        // Alterna entre marcar ou desmarcar uma célula com bandeira
        flagged[row][col] = !flagged[row][col];
        if (flagged[row][col]) {
            buttons[row][col].setText("M");
        } else {
            buttons[row][col].setText("");
        }
    }

    // Método para revelar uma célula
    private void revealCell(int row, int col) {
        // Verifica se a célula já foi revelada
        if (!revealed[row][col]) {
            revealed[row][col] = true;
            int adjacentMines = countAdjacentMines(row, col);

            Button button = buttons[row][col];
            button.setEnabled(false);
            if (adjacentMines > 0) {
                button.setText(String.valueOf(adjacentMines));
            } else {
                revealAdjacentCells(row, col);
            }
        }
    }

    // Método para revelar células ao lado
    private void revealAdjacentCells(int row, int col) {
        for (int i = Math.max(0, row - 1); i <= Math.min(row + 1, SIZE - 1); i++) {
            for (int j = Math.max(0, col - 1); j <= Math.min(col + 1, SIZE - 1); j++) {
                revealCell(i, j);
            }
        }
    }

    // Método para contar bombas próximas
    private int countAdjacentMines(int row, int col) {
        int count = 0;
        for (int i = Math.max(0, row - 1); i <= Math.min(row + 1, SIZE - 1); i++) {
            for (int j = Math.max(0, col - 1); j <= Math.min(col + 1, SIZE - 1); j++) {
                if (mines[i][j]) {
                    count++;
                }
            }
        }
        return count;
    }

    // Método para verificar se o jogador venceu
    private void checkGameWin() {
        boolean win = true;
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (!mines[row][col] && !revealed[row][col]) {
                    win = false;
                    break;
                }
            }
        }

        if (win) {
            // Se o jogador revelou todas as células não bomba, mostra "You Win!"
            revealMines();
            showToast("You Win!");
        }
    }

// Revelar Bombas e Exibir Mensagens

    // Método para revelar todas as bombas no final do jogo
    private void revealMines() {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (mines[row][col]) {
                    buttons[row][col].setText("*");
                }
            }
        }
    }

    // Método para exibir mensagens ao jogador
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        if (message.equals("Game Over!")) {
            // Se o jogo acabou, revela todas as bombas
            revealMines();
        } else if (message.equals("You Win!")) {
            // Se o jogador venceu, reinicia o jogo
            resetGame();
        }
    }

    // Método para reiniciar o jogo
    private void resetGame() {
        // Reinicializa os arrays e configuração do GridLayout
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                mines[row][col] = false;
                revealed[row][col] = false;
                flagged[row][col] = false;

                buttons[row][col].setEnabled(true);
                buttons[row][col].setText("");
            }
        }

        initializeGame();
        setupGridLayout();
    }
}

// agora vou demostrar o .apk em funcionamento. Aceito sugestões para melhoria do projeto.
