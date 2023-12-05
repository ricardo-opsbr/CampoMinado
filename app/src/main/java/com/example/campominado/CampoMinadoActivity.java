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

public class CampoMinadoActivity extends AppCompatActivity {

    private static final int SIZE = 7;
    private static final int MINES = 8; // Mantive o número de bombas como 8 para garantir desafio

    private Button[][] buttons;
    private boolean[][] mines;
    private boolean[][] revealed;
    private boolean[][] flagged;

    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_campo_minado);

        handler = new Handler();

        buttons = new Button[SIZE][SIZE];
        mines = new boolean[SIZE][SIZE];
        revealed = new boolean[SIZE][SIZE];
        flagged = new boolean[SIZE][SIZE];

        GridLayout gridLayout = findViewById(R.id.gridLayout);

        // Obter dimensões da tela
        Display display = getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        int height = display.getHeight();

        // Configurar o tamanho do GridLayout com base nas dimensões da tela
        int gridSize = Math.min(width, height) * 2 / 3; // Usando 2/3 do menor lado da tela
        gridLayout.getLayoutParams().width = gridSize;
        gridLayout.getLayoutParams().height = gridSize;

        gridLayout.setColumnCount(SIZE);

        initializeGame();
        setupGridLayout();

        Button novoJogoButton = findViewById(R.id.btnNovoJogo);
        novoJogoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetGame();
            }
        });
    }

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

    private void setupGridLayout() {
        GridLayout gridLayout = findViewById(R.id.gridLayout);
        gridLayout.removeAllViews(); // Remover views existentes

        int gridSize = gridLayout.getLayoutParams().width; // Usar a largura do GridLayout
        int buttonSize = gridSize / SIZE; // Tamanho dos botões

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                Button button = new Button(this);
                button.setLayoutParams(new GridLayout.LayoutParams());
                button.setTag(new int[]{row, col});
                button.setOnTouchListener(new View.OnTouchListener() {
                    private long initialClickTime;
                    private final long LONG_CLICK_DURATION = 1000;  // Alterado para 1 segundo

                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        int action = motionEvent.getAction();
                        int[] position = (int[]) view.getTag();
                        int row = position[0];
                        int col = position[1];

                        if (action == MotionEvent.ACTION_DOWN) {
                            initialClickTime = SystemClock.uptimeMillis();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    onCellLongClick(row, col);
                                }
                            }, LONG_CLICK_DURATION);
                        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                            handler.removeCallbacksAndMessages(null);
                            if (SystemClock.uptimeMillis() - initialClickTime < LONG_CLICK_DURATION) {
                                onCellClick(row, col);
                            }
                        }
                        return true; // Retorna true para indicar que o evento foi consumido
                    }
                });
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        handler.removeCallbacksAndMessages(null);
                        int[] position = (int[]) view.getTag();
                        onCellClick(position[0], position[1]);
                    }
                });

                buttons[row][col] = button;

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = buttonSize;
                params.height = buttonSize;
                params.setMargins(2, 2, 2, 2); // Margens para separar os botões
                params.rowSpec = GridLayout.spec(row);
                params.columnSpec = GridLayout.spec(col);

                button.setLayoutParams(params);

                gridLayout.addView(button);
            }
        }
    }

    private void onCellClick(int row, int col) {
        if (mines[row][col]) {
            revealMines();
            showToast("Game Over!");
        } else {
            revealCell(row, col);
            checkGameWin();
        }
    }

    private void onCellLongClick(int row, int col) {
        flagged[row][col] = !flagged[row][col];
        if (flagged[row][col]) {
            buttons[row][col].setText("M");
        } else {
            buttons[row][col].setText("");
        }
    }

    private void revealCell(int row, int col) {
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

    private void revealAdjacentCells(int row, int col) {
        for (int i = Math.max(0, row - 1); i <= Math.min(row + 1, SIZE - 1); i++) {
            for (int j = Math.max(0, col - 1); j <= Math.min(col + 1, SIZE - 1); j++) {
                revealCell(i, j);
            }
        }
    }

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
            revealMines();
            showToast("You Win!");
        }
    }

    private void revealMines() {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (mines[row][col]) {
                    buttons[row][col].setText("*");
                }
            }
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        if (message.equals("Game Over!")) {
            revealMines();
        } else if (message.equals("You Win!")) {
            resetGame();
        }
    }

    private void resetGame() {
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
