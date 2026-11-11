import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { Episodio, FiltroGravidade, RespostaAnalise, RespostaErro } from './models/analise.model';
import { ServicoAnalise } from './services/analise.service';

@Component({
  selector: 'app-root',
  imports: [CommonModule, FormsModule],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  private readonly servicoAnalise = inject(ServicoAnalise);
  private readonly detectorMudancas = inject(ChangeDetectorRef);

  protected readonly opcoesGravidade: { valor: FiltroGravidade; rotulo: string }[] = [
    { valor: 'TODAS', rotulo: 'Todas as gravidades' },
    { valor: 'LEVE', rotulo: 'Leve' },
    { valor: 'GRAVE', rotulo: 'Grave' },
    { valor: 'GRAVISSIMA', rotulo: 'Gravíssima' },
  ];

  protected arquivoSelecionado: File | null = null;
  protected resposta: RespostaAnalise | null = null;
  protected filtroGravidade: FiltroGravidade = 'TODAS';
  protected carregando = false;
  protected mensagemErro = '';
  protected mensagemSucesso = '';

  protected get episodiosFiltrados(): Episodio[] {
    if (!this.resposta) {
      return [];
    }

    if (this.filtroGravidade === 'TODAS') {
      return this.resposta.episodios;
    }

    return this.resposta.episodios.filter(
      (episodio) => episodio.gravidade === this.filtroGravidade,
    );
  }

  protected selecionarArquivo(evento: Event): void {
    const campoArquivo = evento.target as HTMLInputElement;
    this.arquivoSelecionado = campoArquivo.files?.[0] ?? null;
    this.resposta = null;
    this.filtroGravidade = 'TODAS';
    this.mensagemErro = '';
    this.mensagemSucesso = '';
  }

  protected analisar(): void {
    if (!this.arquivoSelecionado) {
      this.mensagemErro = 'Selecione um arquivo CSV antes de iniciar a análise.';
      this.mensagemSucesso = '';
      return;
    }

    this.carregando = true;
    this.mensagemErro = '';
    this.mensagemSucesso = '';

    this.servicoAnalise.analisar(this.arquivoSelecionado).subscribe({
      next: (resposta) => {
        this.resposta = resposta;
        this.filtroGravidade = 'TODAS';
        this.mensagemSucesso = 'Arquivo analisado com sucesso.';
        this.carregando = false;
        this.detectorMudancas.markForCheck();
      },
      error: (erro: unknown) => {
        this.resposta = null;
        this.mensagemErro = this.obterMensagemErro(erro);
        this.carregando = false;
        this.detectorMudancas.markForCheck();
      },
    });
  }

  protected formatarTamanhoArquivo(tamanhoBytes: number): string {
    if (tamanhoBytes < 1024) {
      return `${tamanhoBytes} bytes`;
    }

    return `${(tamanhoBytes / 1024).toFixed(1)} KB`;
  }

  private obterMensagemErro(erro: unknown): string {
    if (erro instanceof HttpErrorResponse) {
      const respostaErro = erro.error as Partial<RespostaErro> | null;
      if (respostaErro?.mensagem) {
        return respostaErro.mensagem;
      }
    }

    return 'Não foi possível analisar o arquivo. Verifique se o backend está disponível e tente novamente.';
  }
}
