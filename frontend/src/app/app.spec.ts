import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { App } from './app';
import { RespostaAnalise } from './models/analise.model';

describe('App', () => {
  let controladorHttp: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    controladorHttp = TestBed.inject(HttpTestingController);
  });

  afterEach(() => controladorHttp.verify());

  it('deve criar a aplicação e exibir o título', () => {
    const componente = TestBed.createComponent(App);
    componente.detectChanges();

    expect(componente.componentInstance).toBeTruthy();
    expect(componente.nativeElement.querySelector('h1')?.textContent).toContain(
      'Análise de excesso de velocidade',
    );
  });

  it('deve informar quando nenhum arquivo for selecionado', () => {
    const componente = TestBed.createComponent(App);
    componente.detectChanges();

    const formulario = componente.nativeElement.querySelector('form') as HTMLFormElement;
    formulario.dispatchEvent(new Event('submit'));
    componente.detectChanges();

    expect(componente.nativeElement.querySelector('[role="alert"]')?.textContent).toContain(
      'Selecione um arquivo CSV',
    );
  });

  it('deve exibir o resultado e filtrar episódios sem nova requisição', async () => {
    const componente = TestBed.createComponent(App);
    componente.detectChanges();
    const arquivo = new File(['conteudo'], 'leituras.csv', { type: 'text/csv' });
    selecionarArquivo(componente.nativeElement, arquivo);
    componente.detectChanges();

    const formulario = componente.nativeElement.querySelector('form') as HTMLFormElement;
    formulario.dispatchEvent(new Event('submit'));

    const requisicao = controladorHttp.expectOne('/api/analises');
    requisicao.flush(criarResposta());
    await componente.whenStable();

    expect(componente.nativeElement.textContent).toContain('Arquivo analisado com sucesso.');
    expect(componente.nativeElement.querySelectorAll('.tabela-episodios tbody tr')).toHaveLength(2);

    const filtro = componente.nativeElement.querySelector('#gravidade') as HTMLSelectElement;
    filtro.value = 'GRAVE';
    filtro.dispatchEvent(new Event('change'));
    await componente.whenStable();
    componente.detectChanges();

    expect(componente.nativeElement.textContent).toContain('Exibindo 1 de 2 episódios');
    expect(componente.nativeElement.querySelectorAll('.tabela-episodios tbody tr')).toHaveLength(1);
  });

  it('deve exibir a mensagem enviada pelo backend ao rejeitar o arquivo', async () => {
    const componente = TestBed.createComponent(App);
    componente.detectChanges();
    selecionarArquivo(
      componente.nativeElement,
      new File(['cabecalho incorreto'], 'leituras.csv', { type: 'text/csv' }),
    );
    componente.detectChanges();

    const formulario = componente.nativeElement.querySelector('form') as HTMLFormElement;
    formulario.dispatchEvent(new Event('submit'));

    const requisicao = controladorHttp.expectOne('/api/analises');
    requisicao.flush(
      { mensagem: 'Cabeçalho inválido.' },
      { status: 400, statusText: 'Bad Request' },
    );
    await componente.whenStable();

    expect(componente.nativeElement.querySelector('[role="alert"]')?.textContent).toContain(
      'Cabeçalho inválido.',
    );
  });
});

function selecionarArquivo(elemento: HTMLElement, arquivo: File): void {
  const campoArquivo = elemento.querySelector('#arquivo') as HTMLInputElement;
  Object.defineProperty(campoArquivo, 'files', { value: [arquivo] });
  campoArquivo.dispatchEvent(new Event('change'));
}

function criarResposta(): RespostaAnalise {
  return {
    resumo: { linhasLidas: 3, linhasValidas: 2, totalEpisodios: 2 },
    ranking: [{ placa: 'ABC1D23', totalPontos: 4, maiorVelocidadeKmh: 80 }],
    episodios: [
      {
        placa: 'ABC1D23',
        inicio: '2026-10-05T08:00:00',
        fim: '2026-10-05T08:03:00',
        duracaoSegundos: 180,
        velocidadeMaximaKmh: 80,
        excessoMaximoPercentual: 33.3,
        gravidade: 'GRAVE',
      },
      {
        placa: 'ABC1D23',
        inicio: '2026-10-05T09:00:00',
        fim: '2026-10-05T09:00:00',
        duracaoSegundos: 0,
        velocidadeMaximaKmh: 66,
        excessoMaximoPercentual: 10,
        gravidade: 'LEVE',
      },
    ],
    linhasInvalidas: [{ numeroLinha: 4, motivo: 'velocidadeKmh não pode ser negativa.' }],
  };
}
