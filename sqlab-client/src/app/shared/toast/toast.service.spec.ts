import { TestBed } from '@angular/core/testing';
import { ToastService } from './toast.service';

describe('ToastService', () => {
  let service: ToastService;

  beforeEach(() => {
    vi.useFakeTimers();
    vi.clearAllMocks();
    TestBed.configureTestingModule({});
    service = TestBed.inject(ToastService);
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it('show deve adicionar toast ao signal', () => {
    service.show('Mensagem teste');
    expect(service.toasts().length).toBe(1);
    expect(service.toasts()[0].message).toBe('Mensagem teste');
    expect(service.toasts()[0].type).toBe('info');
  });

  it('show deve aceitar type e duration customizados', () => {
    service.show('Erro', 'error', 5000);
    expect(service.toasts()[0].type).toBe('error');
  });

  it('success deve chamar show com type success', () => {
    service.success('Sucesso!');
    expect(service.toasts()[0].message).toBe('Sucesso!');
    expect(service.toasts()[0].type).toBe('success');
  });

  it('error deve chamar show com type error', () => {
    service.error('Erro!');
    expect(service.toasts()[0].message).toBe('Erro!');
    expect(service.toasts()[0].type).toBe('error');
  });

  it('info deve chamar show com type info', () => {
    service.info('Info!');
    expect(service.toasts()[0].message).toBe('Info!');
    expect(service.toasts()[0].type).toBe('info');
  });

  it('dismiss deve remover toast por ID', () => {
    service.show('Teste');
    const id = service.toasts()[0].id;
    service.dismiss(id);
    expect(service.toasts().length).toBe(0);
  });

  it('auto-dismiss deve remover toast após duration', () => {
    service.show('Auto', 'info', 3000);
    expect(service.toasts().length).toBe(1);

    vi.advanceTimersByTime(3000);
    expect(service.toasts().length).toBe(0);
  });

  it('múltiplos toasts devem aparecer no signal', () => {
    service.show('Primeiro');
    service.show('Segundo');
    service.show('Terceiro');
    expect(service.toasts().length).toBe(3);
  });

  it('dismiss de ID inexistente não deve afetar outros', () => {
    service.show('Primeiro');
    service.show('Segundo');
    service.dismiss('id-inexistente');
    expect(service.toasts().length).toBe(2);
  });
});
