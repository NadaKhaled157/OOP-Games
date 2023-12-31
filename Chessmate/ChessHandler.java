package Chessmate;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class ChessHandler implements ActionListener {
    ChessGUI chessGUI;
    Board chessboard;
    Piece selectedPiece;
    Piece movedPiece;
    Piece eatenPiece;
    Piece castlingKing;
    Piece castlingRook;
    boolean shortCastle;
    boolean longCastle;
    boolean castlingComplete;
    Player whitePlayer= new Player(true,PieceColor.White);
    Player blackPlayer= new Player(false, PieceColor.Black);

    int previousX;
    int previousY;


    public ChessHandler(ChessGUI chessGUI) {
        this.chessGUI = chessGUI;
        this.chessboard = chessGUI.chessboard;
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                chessGUI.buttons[x][y].addActionListener(this);
            }
        }
        whitePlayer.timer= new GameTimer();
        whitePlayer.timer.startTimer(chessGUI);
        blackPlayer.timer=new GameTimer();
    }

    @Override
    public void actionPerformed (ActionEvent e) {
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                if (e.getSource() == chessGUI.buttons[x][y]) {
                    // Selected tile is not empty
                    if (chessboard.boardTiles[x][y].getPiece() != null) {
                        // Player selecting a piece to move
                        if ((isSourceSelected(whitePlayer.isTurn, whitePlayer.playerColor, chessboard.boardTiles[x][y].getPiece().pieceColor))
                                || (isSourceSelected(blackPlayer.isTurn, blackPlayer.playerColor, chessboard.boardTiles[x][y].getPiece().pieceColor))) {
                            selectedPiece = chessboard.boardTiles[x][y].getPiece();
                            //Player deselects a piece
                            if ((!selectedPiece.possibleMoves.isEmpty() || !selectedPiece.illegalMoves.isEmpty())) {
                                    if (selectedPiece instanceof King) {
                                        if (!((King) selectedPiece).isInCheck)
                                            chessGUI.deselectButton(selectedPiece);
                                    }
                                    else if(selectedPiece==castlingRook){
                                        System.out.println("CASTLING COMPLETE");
                                        int m=0;
                                        int n=0;
                                        if(shortCastle) {m= castlingKing.getX()+2; n=castlingRook.getX()-2;}
                                        if(longCastle) {m= castlingKing.getX()-2; n=castlingRook.getX()+3;}
                                        setAndRemovePiece(castlingKing,m,castlingKing.getY());
                                        chessGUI.setAndRemoveImage(castlingKing,null,previousX,previousY);
                                        setAndRemovePiece(castlingRook,n,castlingRook.getY());
                                        chessGUI.setAndRemoveImage(castlingRook,null,previousX,previousY);
                                        castlingComplete=true;
                                        castlingKing=null;
                                        castlingRook=null;
                                    }
                                    else
                                chessGUI.deselectButton(selectedPiece);
                            } else {
                                //selectedPiece.possibleMoves.clear();

                                selectedPiece.findPossibleMoves(chessboard);
//                                chessGUI.disableButtons(selectedPiece);

                                if((blackPlayer.isTurn && !chessboard.blackKing.threatPieces.isEmpty())
                                || (whitePlayer.isTurn && !chessboard.whiteKing.threatPieces.isEmpty())){
//                                    System.out.print("THREAT PIECE: "+chessboard.blackKing.threatPiece.pieceType+" ");
//                                    System.out.print("X"+chessboard.blackKing.threatPiece.getX());
//                                    System.out.println("Y"+chessboard.blackKing.threatPiece.getY());
                                    System.out.println("ENTERED DO NOT ENDANGER CONDITION");
                                    selectedPiece.possibleMoves=doNotEndangerKing(selectedPiece, whitePlayer.isTurn);
//                                    int listSize;
//                                    if(blackPlayer.isTurn){listSize=chessboard.blackKing.threatPieces.size();}
//                                    else{listSize=chessboard.whiteKing.threatPieces.size();}
//                                        for(int i=0;i<listSize;i++){
//                                            Tile threatPieceTile = chessboard.boardTiles[chessboard.blackKing.threatPieces.get(i).getX()][chessboard.blackKing.threatPieces.get(i).getY()];
////                                            doNotEndangerKing(threatPieceTile, chessboard.blackKing.threatPiece);
//                                        }
//                                    else{}
//                                    if (chessboard.blackKing.isInDanger) {
//                                    }
//                                     else {
//                                        chessGUI.disableButtons(selectedPiece);
//                                    }
                                }
                                if (selectedPiece instanceof King) {
                                    ((King) selectedPiece).wouldBeInDanger(chessboard);
                                    chessGUI.disableButtons(selectedPiece);
                                    ((King) selectedPiece).canCastle(chessboard);
                                    if(((King) selectedPiece).canShortCastle) {
                                        System.out.println("SHORT CASTLING ALLOWED");
                                        shortCastle=true;
                                        castlingKing= selectedPiece;
                                        castlingRook=chessboard.boardTiles[selectedPiece.getX() + 3][selectedPiece.getY()].getPiece();
                                        selectedPiece.possibleMoves.add(chessboard.boardTiles[selectedPiece.getX() + 3][selectedPiece.getY()]);
//                                        chessboard.boardTiles[selectedPiece.getX() + 3][selectedPiece.getY()].hasPiece=false;
                                    }
                                    if(((King) selectedPiece).canLongCastle) {
                                        System.out.println("LONG CASTLING ALLOWED");
                                        longCastle=true;
                                        castlingKing= selectedPiece;
                                        castlingRook=chessboard.boardTiles[selectedPiece.getX() -4][selectedPiece.getY()].getPiece();
                                        selectedPiece.possibleMoves.add(chessboard.boardTiles[selectedPiece.getX() -4][selectedPiece.getY()]);
//                                        chessboard.boardTiles[selectedPiece.getX() - 4][selectedPiece.getY()].hasPiece=false;
                                    }
                                }

                                if (selectedPiece instanceof Pawn) {
                                    if (((Pawn) selectedPiece).canPerformEnPassant(chessboard)) {
                                        for (int i = 0;i <((Pawn) selectedPiece).threatenedPawn.length;i++){
                                            Pawn threatenedPawn = ((Pawn) selectedPiece).threatenedPawn[i];
                                            if (threatenedPawn!=null && threatenedPawn.isFirstMove) {
                                                selectedPiece.possibleMoves.add(((Pawn) selectedPiece).enPassantTile[i]);
                                                System.out.println("En Passant Available");
                                            }
                                        }
                                    }
                                }

                                chessGUI.disableButtons(selectedPiece);

//                                System.out.println("--------------SELECTED PIECE INFO----------------");
//                                System.out.println(chessboard.boardTiles[x][y].getPiece().pieceType+" X"+chessboard.boardTiles[x][y].getPiece().getX()+" Y"+
//                                        chessboard.boardTiles[x][y].getPiece().getY());

                                // printing possible moves
                                System.out.println("Possible Moves: "+selectedPiece.possibleMoves.size());
                                for (int i = 0; i < selectedPiece.possibleMoves.size(); i++) {
                                    System.out.print("X" + selectedPiece.possibleMoves.get(i).x + " ");
                                    System.out.println("Y" + selectedPiece.possibleMoves.get(i).y);

                                }
                                //printing illegal moves
                                System.out.println("Illegal Moves: "+selectedPiece.illegalMoves.size());
                                for (int i = 0; i < selectedPiece.illegalMoves.size(); i++) {
                                    System.out.print("X" + selectedPiece.illegalMoves.get(i).x + " ");
                                    System.out.println("Y" + selectedPiece.illegalMoves.get(i).y);

                                }
                                System.out.println("---------------------------------------------------------");
                                //Highlighting and enabling possible moves of selected piece
                                chessGUI.setAndRemoveHighlight(selectedPiece, x, y);
//                                chessGUI.disableButtons(selectedPiece);
                                movedPiece = selectedPiece;
                            }
                        } else {
                            // Player selecting a piece out of turn
                            selectedPiece = chessboard.boardTiles[x][y].getPiece();
                            if (chessboard.boardTiles[x][y].hasPiece && !isAttackingOpponentPiece(x, y)) {
                                System.out.println("not your turn");
                                JOptionPane.showMessageDialog(null, "NOT YOUR TURN");
                            }
                        }
                    }
                    // Player selecting a destination for their piece
                    if (chessboard.boardTiles[x][y].isHighlighted) {
                        //Attacking opponent's piece
                        if (chessboard.boardTiles[x][y].hasPiece) {
                            System.out.println("reached before attack condition");
                            if (isAttackingOpponentPiece(x, y)) {
                                System.out.println("OPPONENT ATTACKED");
                                eatenPiece = chessboard.boardTiles[x][y].getPiece();
                                setAndRemovePiece(movedPiece, x, y);
                                chessGUI.setAndRemoveImage(movedPiece, eatenPiece, previousX, previousY);
                            }
//                            if (selectedPiece instanceof King && chessboard.boardTiles[x][y].getPiece() instanceof Rook rook){
//                                chessGUI.runCastlingGUI(selectedPiece, rook, ((King) selectedPiece).canShortCastle, ((King) selectedPiece).canLongCastle);
//                                System.out.println("CASTLING SUCCESSFUL");
//                            }
                        } else if(!castlingComplete) {
                            setAndRemovePiece(movedPiece, x, y);
                            chessGUI.setAndRemoveImage(movedPiece, null, previousX, previousY);
                        }
                        castlingComplete=false;
                        //Moving to an empty tile
                        System.out.println(movedPiece.pieceType);

                        chessGUI.setAndRemoveHighlight(movedPiece, previousX, previousY);
                        chessGUI.enableButtons();
                        whitePlayer.changeTurn();
                        blackPlayer.changeTurn();
                        //Clear a piece's possible moves after it is moved
                        movedPiece.possibleMoves.clear();
                        movedPiece.illegalMoves.clear();
                        if (movedPiece instanceof Pawn){
                            if (((Pawn) movedPiece).canPromote(chessboard)) {
                            chessGUI.runPromotionGUI(movedPiece.pieceColor);
                            Action bishopPromotion = new AbstractAction() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    Bishop bishopPiece = new Bishop(movedPiece.x, movedPiece.y, movedPiece.pieceColor);
                                    handlePromotion(movedPiece, bishopPiece);
                                    System.out.println("BISHOP PROMOTION");
                                    chessGUI.promotionFrame.dispose();
                                }
                            };
                            Action rookPromotion = new AbstractAction() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    Rook rookPiece = new Rook(movedPiece.x, movedPiece.y, movedPiece.pieceColor);
                                    handlePromotion(movedPiece, rookPiece);
                                    System.out.println("ROOK PROMOTION");
                                    chessGUI.promotionFrame.dispose();
                                }
                            };

                            Action knightPromotion = new AbstractAction() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    Knight knightPiece = new Knight(movedPiece.x, movedPiece.y, movedPiece.pieceColor);
                                    handlePromotion(movedPiece, knightPiece);
                                    System.out.println("KNIGHT PROMOTION");
                                    chessGUI.promotionFrame.dispose();
                                }
                            };
                            Action queenPromotion = new AbstractAction() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    Queen queenPiece = new Queen(movedPiece.x, movedPiece.y, movedPiece.pieceColor);
                                    handlePromotion(movedPiece, queenPiece);
                                    System.out.println("QUEEN PROMOTION");
                                    chessGUI.promotionFrame.dispose();
                                }
                            };
                            chessGUI.bishopButton.addActionListener(bishopPromotion);
                            chessGUI.rookButton.addActionListener(rookPromotion);
                            chessGUI.knightButton.addActionListener(knightPromotion);
                            chessGUI.queenButton.addActionListener(queenPromotion);

                        }
                            if ((movedPiece.pieceColor.equals(PieceColor.White) && previousY==1) ||
                                    (movedPiece.pieceColor.equals(PieceColor.Black) && previousY==6))
                                ((Pawn) movedPiece).isFirstMove=true;
                            else
                                ((Pawn) movedPiece).isFirstMove=false;

                            for(int i=0;i<((Pawn) selectedPiece).threatenedPawn.length;i++) {
                                if (chessboard.boardTiles[movedPiece.getX()][movedPiece.getY()] == ((Pawn) selectedPiece).enPassantTile[i]) {
                                    handleEnPassant(((Pawn) selectedPiece).threatenedPawn[i]);
                                }
                                if (chessboard.boardTiles[movedPiece.getX()][movedPiece.getY()] == ((Pawn) selectedPiece).enPassantTileTwo) {
                                    handleEnPassant(((Pawn) selectedPiece).threatenedPawnTwo);
                                }
                            }
                    }
                        if(movedPiece instanceof King){
                            ((King) movedPiece).isFirstMove = false;
                        }
//                                chessboard.whiteKing.isInCheck(chessboard);
//                                chessboard.blackKing.isInCheck(chessboard);
//                        chessboard.whiteKing.isInCheck=false;
//                        chessboard.blackKing.isInCheck=false;
                        chessboard.whiteKing.wouldBeInDanger(chessboard);
                        chessboard.blackKing.wouldBeInDanger(chessboard);

                        if((chessboard.whiteKing.isInCheck(chessboard)&& whitePlayer.isTurn) ||
                                (chessboard.blackKing.isInCheck(chessboard)&& blackPlayer.isTurn)){
//                            chessboard.whiteKing.possibleMoves.clear();
//                            chessboard.blackKing.possibleMoves.clear();
//                            chessboard.whiteKing.findPossibleMoves(chessboard);
//                            chessboard.blackKing.findPossibleMoves(chessboard);
//                            chessboard.whiteKing.wouldBeInDanger(chessboard);
//                            chessboard.blackKing.wouldBeInDanger(chessboard);

                            chessGUI.runKingInCheckGUI(chessboard,movedPiece,whitePlayer,blackPlayer);
                            chessboard.whiteKing.isCheckmated(chessboard);
                            chessboard.blackKing.isCheckmated(chessboard);
//
                            System.out.println("White Checkmate: "+ chessboard.whiteKing.isCheckmated);
                            System.out.println("Black Checkmate: "+ chessboard.blackKing.isCheckmated);

                            if(chessboard.whiteKing.isCheckmated(chessboard)){
                                chessGUI.runGameOverGUI(PieceColor.White);
                            }
                            if (chessboard.blackKing.isCheckmated(chessboard)){
                                chessGUI.runGameOverGUI(PieceColor.Black);
                            }
                        }
//                        else if ((chessboard.whiteKing.isInCheck(chessboard)&& blackPlayer.isTurn)){
//                            chessGUI.runGameOverGUI(PieceColor.White);
//                        } else if ((chessboard.blackKing.isInCheck(chessboard)&& whitePlayer.isTurn)){
//                            chessGUI.runGameOverGUI(PieceColor.Black);
//                        }
                        System.out.println("White King: "+chessboard.whiteKing.isInCheck);
                        System.out.println("Black King: "+chessboard.blackKing.isInCheck);
                        //Clear all possible moves after checking both king's condition
                        //to allow for deselection to take place normally
                        //as it depends on the presence of possible and illegal moves
                        chessboard.whiteKing.possibleMoves.clear();
                        chessboard.whiteKing.illegalMoves.clear();
                        chessboard.blackKing.possibleMoves.clear();
                        chessboard.blackKing.illegalMoves.clear();

                        System.out.println("New Tile: " + chessboard.boardTiles[x][y].getPiece().pieceType);
                        System.out.println("Old Tile: " + chessboard.boardTiles[previousX][previousY].getPiece());
                    }
                }
            }
        }
    }

    public void handleEnPassant(Pawn threatenedPawn) {
        chessboard.boardTiles[threatenedPawn.getX()][threatenedPawn.getY()].setPiece(null);
        chessboard.boardTiles[threatenedPawn.getX()][threatenedPawn.getY()].hasPiece = false;
        System.out.println("PreviousX" + previousX);
        System.out.println("PreviousY" + previousY);
        chessGUI.setAndRemoveImage(null, threatenedPawn, previousX, previousY);
        chessGUI.buttons[threatenedPawn.getX()][threatenedPawn.getY()].setIcon(null);
        System.out.println("EN PASSANT SUCCESSFUL");
    }


    public boolean isSourceSelected (boolean isTurn, PieceColor playerColor, PieceColor selectedPieceColor){
        return isTurn && playerColor.toString().equals(selectedPieceColor.toString());
    }

    public boolean isAttackingOpponentPiece(int x,int y){
        if (chessboard.boardTiles[x][y].isHighlighted)
            //white player selecting black piece or
            // black player selecting white piece
            return whitePlayer.isTurn && chessboard.boardTiles[x][y].getPiece().pieceColor.toString().equals(PieceColor.Black.toString())
                    || (blackPlayer.isTurn && chessboard.boardTiles[x][y].getPiece().pieceColor.toString().equals(PieceColor.White.toString()));
        return false;
    }

    public void setAndRemovePiece(Piece movedPiece, int x, int y){
        //removing piece from previous tile
        previousX= movedPiece.x;
        previousY=movedPiece.y;
        chessboard.boardTiles[previousX][previousY].setPiece(null);
        chessboard.boardTiles[previousX][previousY].hasPiece=false;

        //adding piece to new tile
        chessboard.boardTiles[x][y].setPiece(movedPiece);
        chessboard.boardTiles[x][y].hasPiece=true;
        movedPiece.setX(x);
        movedPiece.setY(y);
    }
    public void handlePromotion(Piece movedPiece, Piece promotedPiece){
        int newX= movedPiece.x;
        int newY= movedPiece.y;
        //this.chessGUI.chessboard.boardTiles[newX][newY].setPiece(null);
        this.chessGUI.chessboard.boardTiles[newX][newY].setPiece(promotedPiece);
        String filename= "resources/Photos/Chess Photos/Chess Pieces/"+promotedPiece.pieceColor.toString()+" "+promotedPiece.pieceType.toString()+".png";
        System.out.println(filename);
        ImageIcon icon=new ImageIcon(filename);
        this.chessGUI.buttons[newX][newY].setIcon(icon);
    }

//    public void handleCastling(Piece king, Piece rook, boolean shortCastle, boolean longCastle){
//        System.out.print("king X"+king.getX()+" Y"+king.getY());
//        System.out.print("rook X"+rook.getX()+" Y"+rook.getY());
//        this.chessboard.boardTiles[king.getX()][king.getY()].setPiece(null);
//        this.chessboard.boardTiles[king.getX()][king.getY()].hasPiece=false;
//        this.chessboard.boardTiles[rook.getX()][rook.getY()].setPiece(null);
//        this.chessboard.boardTiles[rook.getX()][rook.getY()].hasPiece=false;
//
//        if (shortCastle && rook.getX()==7){
//            this.chessboard.boardTiles[6][7].setPiece(king);
//            this.chessboard.boardTiles[6][7].hasPiece=true;
//            king.setX(6);
//
//            this.chessboard.boardTiles[5][7].setPiece(rook);
//            this.chessboard.boardTiles[5][7].hasPiece=true;
//            rook.setX(5);
//        }
//        if(longCastle && rook.getX()==0){
//            this.chessboard.boardTiles[2][7].setPiece(king);
//            this.chessboard.boardTiles[2][king.getY()].hasPiece=true;
//            king.setX(2);
//
//            this.chessboard.boardTiles[3][rook.getY()].setPiece(rook);
//            this.chessboard.boardTiles[3][rook.getY()].hasPiece=true;
//            rook.setX(3);
//        }
//    }

    //This method is used to prevent a piece protecting the king from moving
    //and possibly putting the king in danger of being checkmated.
    //The piece is only allowed to move if it can eat the threatening piece
    public ArrayList<Tile> doNotEndangerKing(Piece selectedPiece, boolean isWhiteTurn){
        Board tempBoard= new Board();
        for(int x=0;x<7;x++){
            for (int y=0;y<7;y++){
                if (chessboard.boardTiles[x][y].hasPiece){
                    tempBoard.boardTiles[x][y].setPiece(chessboard.boardTiles[x][y].getPiece());
                    tempBoard.boardTiles[x][y].hasPiece=true;
                } else {
                    tempBoard.boardTiles[x][y].setPiece(null);
                    tempBoard.boardTiles[x][y].hasPiece=false;
                }
            }
        }
        Piece tempSelectedPiece=null;
        PieceType tempPieceType=selectedPiece.pieceType;
        PieceColor tempPieceColor=selectedPiece.pieceColor;
        int constantX= selectedPiece.getX();
        int constantY= selectedPiece.getY();
        switch (tempPieceType) {
            case Pawn -> tempSelectedPiece = new Pawn(constantX, constantY, tempPieceColor);
            case Rook -> tempSelectedPiece = new Rook(constantX, constantY, tempPieceColor);
            case Knight -> tempSelectedPiece = new Knight(constantX, constantY, tempPieceColor);
            case Bishop -> tempSelectedPiece = new Bishop(constantX, constantY, tempPieceColor);
            case Queen -> tempSelectedPiece = new Queen(constantX, constantY, tempPieceColor);
            case King -> {
                tempSelectedPiece = new King(constantX, constantY, tempPieceColor);
                if (tempPieceColor.equals(PieceColor.White)) {
                    tempBoard.whiteKing = (King) tempSelectedPiece;
                } else {
                    tempBoard.blackKing = (King) tempSelectedPiece;
                }
            }
        }
        for(int k=0;k<selectedPiece.possibleMoves.size();k++){
            tempSelectedPiece.possibleMoves.add(tempBoard.boardTiles[selectedPiece.possibleMoves.get(k).getX()][selectedPiece.possibleMoves.get(k).getY()]);
        }
//        tempSelectedPiece.possibleMoves=selectedPiece.possibleMoves;
        System.out.println("Temp Possible Moves: "+tempSelectedPiece.possibleMoves.size());
        for (int k = 0; k < tempSelectedPiece.possibleMoves.size(); k++) {
            System.out.print("X" + tempSelectedPiece.possibleMoves.get(k).x + " ");
            System.out.println("Y" + tempSelectedPiece.possibleMoves.get(k).y);
        }
        for (int i=0;i<tempSelectedPiece.possibleMoves.size();i++){
            int testX=tempSelectedPiece.possibleMoves.get(i).getX();
            int testY=tempSelectedPiece.possibleMoves.get(i).getY();
//            int constantX= tempSelectedPiece.getX();
//            int constantY= tempSelectedPiece.getY();

            tempBoard.boardTiles[constantX][constantY].setPiece(null);
            tempBoard.boardTiles[constantX][constantY].hasPiece=false;
            tempBoard.boardTiles[testX][testY].setPiece(tempSelectedPiece);
            tempBoard.boardTiles[testX][testY].hasPiece=true;
            tempSelectedPiece.setX(testX);
            tempSelectedPiece.setY(testY);
//            System.out.println("test X "+testX+" Y"+testY);
//            System.out.println("selected Y"+selectedPiece.getX()+" Y"+ selectedPiece.getY());


            if(isWhiteTurn) {
                System.out.println("TESTING X"+testX+" Y"+testY);
                if (tempBoard.whiteKing.isInCheck(tempBoard)) {
                    selectedPiece.possibleMoves.remove(chessboard.boardTiles[testX][testY]);

                    System.out.println("REMOVED X"+testX+" Y"+testY);
                    System.out.println("New Possible Moves: "+selectedPiece.possibleMoves.size());
                    for (int k = 0; k < selectedPiece.possibleMoves.size(); k++) {
                        System.out.print("X" + selectedPiece.possibleMoves.get(k).x + " ");
                        System.out.println("Y" + selectedPiece.possibleMoves.get(k).y);
                    }
                }
            } else {
                System.out.println("TESTING X"+testX+" Y"+testY);
                if (tempBoard.blackKing.isInCheck(tempBoard)) {
                    selectedPiece.possibleMoves.remove(chessboard.boardTiles[testX][testY]);
                    selectedPiece.illegalMoves.add(chessboard.boardTiles[testX][testY]);
                    System.out.println("REMOVED X"+testX+" Y"+testY);

                    System.out.println("New Possible Moves: "+selectedPiece.possibleMoves.size());
                    for (int k = 0; k < selectedPiece.possibleMoves.size(); k++) {
                        System.out.print("X" + selectedPiece.possibleMoves.get(k).x + " ");
                        System.out.println("Y" + selectedPiece.possibleMoves.get(k).y);
                    }

                }
            }

            tempBoard.boardTiles[constantX][constantY].setPiece(tempSelectedPiece);
            tempBoard.boardTiles[constantX][constantY].hasPiece=true;
            tempBoard.boardTiles[testX][testY].setPiece(null);
            tempBoard.boardTiles[testX][testY].hasPiece=false;
            tempSelectedPiece.setX(constantX);
            tempSelectedPiece.setY(constantY);
            tempBoard.blackKing.isInCheck=false;
            tempBoard.whiteKing.isInCheck=false;
        }

        System.out.println("Final Possible Moves: "+selectedPiece.possibleMoves.size());
        for (int k = 0; k < selectedPiece.possibleMoves.size(); k++) {
            System.out.print("X" + selectedPiece.possibleMoves.get(k).x + " ");
            System.out.println("Y" + selectedPiece.possibleMoves.get(k).y);
        }

    return selectedPiece.possibleMoves;
    }
//    public ArrayList<Tile> doNotEndangerKing(Board chessboard ,Piece selectedPiece,boolean isWhiteTurn) {
//        Board tempBoard= new Board();
//        Piece tempSelectedPiece;
//        int constantX;
//        int constantY;
////        for(int x=0; x<8;x++) {
////            for (int y = 0; y < 8; y++) {
////                if (chessboard.boardTiles[x][y].getPiece() != null) {
//////                    Piece copyPiece=null;
//////                    PieceType copyPieceType = chessboard.boardTiles[x][y].getPiece().pieceType;
//////                    PieceColor copyPieceColor = chessboard.boardTiles[x][y].getPiece().pieceColor;
//////                    switch (copyPieceType) {
//////                        case Pawn -> copyPiece = new Pawn(x, y, copyPieceColor);
//////                        case Rook -> copyPiece = new Rook(x, y, copyPieceColor);
//////                        case Knight -> copyPiece = new Knight(x, y, copyPieceColor);
//////                        case Bishop -> copyPiece = new Bishop(x, y, copyPieceColor);
//////                        case Queen -> copyPiece = new Queen(x, y, copyPieceColor);
//////                        case King -> {
//////                            copyPiece = new King(x, y, copyPieceColor);
//////                            if (copyPieceColor.equals(PieceColor.White)) {
//////                                tempBoard.whiteKing = (King) copyPiece;
//////                            } else {
//////                                tempBoard.blackKing = (King) copyPiece;
//////                            }
//////                        }
//////                    }
//////                    tempBoard.boardTiles[x][y].setPiece(copyPiece);
////                    tempBoard.boardTiles[x][y].setPiece(chessboard.boardTiles[x][y].getPiece());
////                    tempBoard.boardTiles[x][y].hasPiece=true;
////                } else {
////                    tempBoard.boardTiles[x][y].setPiece(null);
////                    tempBoard.boardTiles[x][y].hasPiece=false;
////                }
////            }
////        }
//        tempSelectedPiece=tempBoard.boardTiles[selectedPiece.getX()][selectedPiece.getY()].getPiece();
//        //tempSelectedPiece.findPossibleMoves(tempBoard);
//
//        for (int i=0;i<selectedPiece.possibleMoves.size();i++) {
//            int testX = selectedPiece.possibleMoves.get(i).getX();
//            int testY = selectedPiece.possibleMoves.get(i).getY();
//
//            constantX = selectedPiece.x;
//            constantY = selectedPiece.y;
//            tempBoard.boardTiles[constantX][constantY].setPiece(null);
//            tempBoard.boardTiles[constantY][constantY].hasPiece = false;
//
//            tempBoard.boardTiles[testX][testY].setPiece(tempSelectedPiece);
//            tempBoard.boardTiles[testX][testY].hasPiece = true;
//            tempSelectedPiece.setX(testX);
//            tempSelectedPiece.setY(testY);
//
//            if (isWhiteTurn) {
//                if (tempBoard.whiteKing.isInCheck(tempBoard)) {
//                    selectedPiece.possibleMoves.remove(chessboard.boardTiles[testX][testY]);
//                }
//            } else {
//                if (tempBoard.blackKing.isInCheck(tempBoard)) {
//                    selectedPiece.possibleMoves.remove(chessboard.boardTiles[testX][testY]);
//                }
//            }
//
//            tempBoard.boardTiles[constantX][constantY].setPiece(tempSelectedPiece);
//            tempBoard.boardTiles[constantY][constantY].hasPiece = true;
//            tempBoard.boardTiles[testX][testY].setPiece(null);
//            tempBoard.boardTiles[testX][testY].hasPiece = false;
//            tempSelectedPiece.setX(constantX);
//            tempSelectedPiece.setY(constantY);
//        }
////        chessGUI.buttons[selectedPiece.getX()][selectedPiece.getY()].setEnabled(true);
////        chessGUI.disableButtons(selectedPiece);
//
//        /*--------------------------------------------------------------------*/
//        /*--------------------------------------------------------------------*/
////        int listSize;
////        ArrayList<Piece> threatPiecesList=new ArrayList<>();
////        Tile threatPieceTile;
////        Piece threatPiece;
////        King king;
////        if (blackPlayer.isTurn) {
////            listSize = chessboard.blackKing.threatPieces.size();
////            king=chessboard.blackKing;
////            threatPiecesList=chessboard.blackKing.threatPieces;
////        } else {
////            listSize = chessboard.whiteKing.threatPieces.size();
////            king= chessboard.whiteKing;
////            threatPiecesList=chessboard.whiteKing.threatPieces;
////        }
////        for (int k = 0; k < listSize; k++) {
////            threatPieceTile = chessboard.boardTiles[chessboard.blackKing.threatPieces.get(k).getX()][chessboard.blackKing.threatPieces.get(k).getY()];
////            threatPiece=chessboard.boardTiles[threatPieceTile.getX()][threatPieceTile.getY()].getPiece();
////
////            if (!selectedPiece.possibleMoves.contains(threatPieceTile) &&
////                    threatPiece.possibleMoves.contains(chessboard.boardTiles[selectedPiece.getX()][selectedPiece.getY()])) {
////                System.out.println("Clear all moves condition");
////                chessGUI.enableButtons();
////                selectedPiece.possibleMoves.clear();
//////                break;
////            } else if (selectedPiece instanceof Rook || selectedPiece instanceof Queen) {
////                selectedPiece.possibleMoves.clear();
////                //UP
////                System.out.println("Selected Piece: X"+selectedPiece.getX()+" Y"+selectedPiece.getY());
////                System.out.println("Threat Piece: X"+threatPiecesList.get(k).getX()+" Y"+threatPiecesList.get(k).getY());
////                System.out.println("King Piece: X"+king.getX()+" Y"+king.getY());
////                if(selectedPiece.getY()< threatPiecesList.get(k).getY() && selectedPiece.getY()> king.getY()){
////                    System.out.println("UP");
////                    for(int i= king.getY()+1;i <= threatPiecesList.get(k).getY();i++)
////                        selectedPiece.possibleMoves.add(chessboard.boardTiles[selectedPiece.getX()][i]);
////                    break;
////                }
////                //DOWN
////                if(selectedPiece.getY()> threatPiecesList.get(k).getY() && selectedPiece.getY()< king.getY()){
////                    System.out.println("DOWN");
////                    for(int i= threatPiecesList.get(k).getY();i < king.getY();i++)
////                        selectedPiece.possibleMoves.add(chessboard.boardTiles[selectedPiece.getX()][i]);
////                    break;
////                }
////                //RIGHT
////                if(selectedPiece.getX()< threatPiecesList.get(k).getX() && selectedPiece.getX()> king.getX()){
////                    System.out.println("RIGHT");
////                    for(int i= king.getX()+1; i<= threatPiecesList.get(k).getX();i++)
////                        selectedPiece.possibleMoves.add(chessboard.boardTiles[i][selectedPiece.getY()]);
////                    break;
////                }
////                //LEFT
////                if(selectedPiece.getX()> threatPiecesList.get(k).getX() && selectedPiece.getX()< king.getX()){
////                    System.out.println("LEFT");
////                    for(int i= threatPiecesList.get(k).getX();i< king.getX();i++)
////                        selectedPiece.possibleMoves.add(chessboard.boardTiles[i][selectedPiece.getY()]);
////                    break;
////                }
//
//        /*--------------------------------------------------------------------*/
//        /*--------------------------------------------------------------------*/
//
////                //UP
////                for (int i = 1; selectedPiece.getY() + i <= threatPiece.getY(); i++) {
////                    selectedPiece.possibleMoves.add(chessboard.boardTiles[selectedPiece.getX()][selectedPiece.getY() + i]);
////                }
////                //DOWN
////                for (int i = 1; selectedPiece.getY() - i >= threatPiece.getY(); i++) {
////                    selectedPiece.possibleMoves.add(chessboard.boardTiles[selectedPiece.getX()][selectedPiece.getY() - i]);
////                }
////                //RIGHT
////                for (int i = 1; selectedPiece.getX() + i <= threatPiece.getX(); i++) {
////                    selectedPiece.possibleMoves.add(chessboard.boardTiles[selectedPiece.getX() + i][selectedPiece.getY()]);
////                }
////                //LEFT
////                for (int i = 1; selectedPiece.getX() - i >= threatPiece.getX(); i++) {
////                    selectedPiece.possibleMoves.add(chessboard.boardTiles[selectedPiece.getX() - i][selectedPiece.getY()]);
////                }
////                chessGUI.disableButtons(selectedPiece);
////            }
////            else if (selectedPiece instanceof Bishop || selectedPiece instanceof Queen) {
////                //UP RIGHT
////                int i = selectedPiece.getX() + 1;
////                int j = selectedPiece.getY() + 1;
////                while (i <= threatPiece.getX() && j <= threatPiece.getY()) {
////                    selectedPiece.possibleMoves.add(chessboard.boardTiles[i][j]);
////                    i++;
////                    j++;
////                }
////                //UP LEFT
////                i = selectedPiece.getX() - 1;
////                j = selectedPiece.getY() + 1;
////                while (i >= threatPiece.getX() && j <= threatPiece.getY()) {
////                    selectedPiece.possibleMoves.add(chessboard.boardTiles[i][j]);
////                    i--;
////                    j++;
////                }
////                //DOWN RIGHT
////                i = selectedPiece.getX() + 1;
////                j = selectedPiece.getY() - 1;
////                while (i <= threatPiece.getX() && j >= threatPiece.getY()) {
////                    selectedPiece.possibleMoves.add(chessboard.boardTiles[i][j]);
////                    i++;
////                    j--;
////                }
////                //DOWN LEFT
////                i = selectedPiece.getX() - 1;
////                j = selectedPiece.getY() - 1;
////                while (i >= threatPiece.getX() && j >= threatPiece.getY()) {
////                    selectedPiece.possibleMoves.add(chessboard.boardTiles[i][j]);
////                    i--;
////                    j--;
////                }
//
////            } else if (selectedPiece instanceof Knight) {
////                selectedPiece.possibleMoves.clear();
////                selectedPiece.possibleMoves.add(threatPieceTile);
////            }
////        }
////        chessGUI.disableButtons(selectedPiece);
//        return selectedPiece.possibleMoves;
//    }
}




















//    public ArrayList<Tile> doNotEndangerKing(Piece selectedPiece,boolean isWhiteTurn) {
//        Board tempBoard= new Board();
////        System.out.println(tempSelectedPiece.pieceType);
//        for(int x=0; x<8;x++){
//            for(int y=0;y<8;y++) {
//                if (chessboard.boardTiles[x][y].getPiece() != null) {
//                    tempBoard.boardTiles[x][y].setPiece(chessboard.boardTiles[x][y].getPiece());
//                    tempBoard.boardTiles[x][y].hasPiece=true;
//                } else {
//                    tempBoard.boardTiles[x][y].setPiece(null);
//                    tempBoard.boardTiles[x][y].hasPiece=false;
//                }
//            }
//        }
//    } else {
//        tempBoard.boardTiles[x][y].setPiece(null);
//        tempBoard.boardTiles[x][y].hasPiece=false;
//        }
//        }
//        }
//        Piece tempSelectedPiece=tempBoard.boardTiles[selectedPiece.getX()][selectedPiece.getY()].getPiece();
//
//        for (int i=0;i<selectedPiece.possibleMoves.size();i++) {
//        int testX=selectedPiece.possibleMoves.get(0).getX();
//        int testY=selectedPiece.possibleMoves.get(0).getY();
//
//        constantX= selectedPiece.x;
//        constantY=selectedPiece.y;
//        tempBoard.boardTiles[constantX][constantY].setPiece(null);
//        tempBoard.boardTiles[constantY][constantY].hasPiece=false;
//
//        tempBoard.boardTiles[testX][testY].setPiece(movedPiece);
//        tempBoard.boardTiles[testX][testY].hasPiece=true;
//        tempSelectedPiece.setX(testX);
//        tempSelectedPiece.setY(testY);
//
//        if(isWhiteTurn) {
//        if (tempBoard.whiteKing.isInCheck(tempBoard)) {
//        selectedPiece.possibleMoves.remove(chessboard.boardTiles[testX][testY]);
//        }
//        } else {
//        if (tempBoard.blackKing.isInCheck(tempBoard)) {
//        selectedPiece.possibleMoves.remove(chessboard.boardTiles[testX][testY]);
//        }
//        }
//
//        //Return piece to its place before the next check.
//        tempBoard.boardTiles[constantX][constantY].setPiece(selectedPiece);
//        tempBoard.boardTiles[constantX][constantY].hasPiece=true;
//        tempBoard.boardTiles[testX][testY].setPiece(null);
//        tempBoard.boardTiles[testX][testY].hasPiece=false;
//        tempSelectedPiece.setX(constantX);
//        tempSelectedPiece.setY(constantY);
//
//        tempBoard.whiteKing.isInCheck=false;
//        tempBoard.blackKing.isInCheck=false;
//        chessboard.whiteKing.isInCheck=false;
//        chessboard.blackKing.isInCheck=false;
//
//
////            setAndRemovePiece(tempBoard,selectedPiece,testX,testY);
//////            tempBoard.boardTiles[selectedPiece.getX()][selectedPiece.getY()].setPiece(null);
//////            tempBoard.boardTiles[selectedPiece.possibleMoves.get(0).getX()][selectedPiece.possibleMoves.get(0).getY()].setPiece(selectedPiece);
////            if(isWhiteTurn) {
////                if (tempBoard.whiteKing.isInCheck(tempBoard)) {
////                    selectedPiece.possibleMoves.remove(chessboard.boardTiles[testX][testY]);
////                }
////            } else {
////                if (tempBoard.blackKing.isInCheck(tempBoard)) {
////                    selectedPiece.possibleMoves.remove(chessboard.boardTiles[testX][testY]);
////                }
////            }
//        }
