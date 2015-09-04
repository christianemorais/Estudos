package com.chrismorais.calculatec;

public class Calculadora {
	private double operando;
	private double operandoAnterior;
	private String operadorAnterior;

	public Calculadora(){
		operando = 0;
		operandoAnterior = 0;
		operadorAnterior = "";
	}

	public double getOperando() {
		return operando;
	}

	public void setOperando(double operando) {
		this.operando = operando;
	}

	public void realizarOperacao(String operacao){
		realizarOperacaoAnterior();

		operandoAnterior = operando;
		operadorAnterior = operacao;
	}

	private void realizarOperacaoAnterior() {
		switch (operadorAnterior) {
			case "+": operando += operandoAnterior;
				break;
			case "-": operando -= operandoAnterior;
				break;
			case "x": operando = operandoAnterior * operando;
				break;
			case "÷": {
				if (operando != 0) {
					operando  = operandoAnterior / operando;
				}

				break;
			}
		}
	}
}
