import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { ServicoAnalise } from './analise.service';

describe('ServicoAnalise', () => {
  let servico: ServicoAnalise;
  let controladorHttp: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ServicoAnalise, provideHttpClient(), provideHttpClientTesting()],
    });

    servico = TestBed.inject(ServicoAnalise);
    controladorHttp = TestBed.inject(HttpTestingController);
  });

  afterEach(() => controladorHttp.verify());

  it('deve enviar o arquivo no campo multipart esperado pela API', () => {
    const arquivo = new File(['conteudo'], 'leituras.csv', { type: 'text/csv' });

    servico.analisar(arquivo).subscribe();

    const requisicao = controladorHttp.expectOne('/api/analises');
    expect(requisicao.request.method).toBe('POST');
    expect(requisicao.request.body).toBeInstanceOf(FormData);
    expect((requisicao.request.body as FormData).get('arquivo')).toBe(arquivo);
    expect(requisicao.request.headers.has('Content-Type')).toBe(false);
    requisicao.flush({ resumo: {}, episodios: [], ranking: [], linhasInvalidas: [] });
  });
});
