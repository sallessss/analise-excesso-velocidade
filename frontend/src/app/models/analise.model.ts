export type Gravidade = 'LEVE' | 'GRAVE' | 'GRAVISSIMA';
export type FiltroGravidade = 'TODAS' | Gravidade;

export interface ResumoAnalise {
  linhasLidas: number;
  linhasValidas: number;
  totalEpisodios: number;
}

export interface Episodio {
  placa: string;
  inicio: string;
  fim: string;
  duracaoSegundos: number;
  velocidadeMaximaKmh: number;
  excessoMaximoPercentual: number;
  gravidade: Gravidade;
}

export interface PosicaoRanking {
  placa: string;
  totalPontos: number;
  maiorVelocidadeKmh: number;
}

export interface LinhaInvalida {
  numeroLinha: number;
  motivo: string;
}

export interface RespostaAnalise {
  resumo: ResumoAnalise;
  episodios: Episodio[];
  ranking: PosicaoRanking[];
  linhasInvalidas: LinhaInvalida[];
}

export interface RespostaErro {
  mensagem: string;
}
