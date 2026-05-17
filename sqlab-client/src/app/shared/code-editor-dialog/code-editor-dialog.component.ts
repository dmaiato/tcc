import { Component, input, output, HostListener, viewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NgIconsModule } from '@ng-icons/core';

@Component({
  selector: 'app-code-editor-dialog',
  standalone: true,
  imports: [CommonModule, NgIconsModule],
  templateUrl: './code-editor-dialog.component.html',
  styleUrl: './code-editor-dialog.component.css'
})
export class CodeEditorDialogComponent {
  readonly title = input.required<string>();
  readonly value = input.required<string>();
  readonly valueChange = output<string>();
  readonly close = output<void>();

  private readonly textareaRef = viewChild<ElementRef<HTMLTextAreaElement>>('textarea');

  onInput(event: Event): void {
    this.valueChange.emit((event.target as HTMLTextAreaElement).value);
  }

  onKeyDown(event: KeyboardEvent): void {
    if (event.key === 'Tab') {
      event.preventDefault();
      const textarea = this.textareaRef()?.nativeElement;
      if (!textarea) return;

      const start = textarea.selectionStart;
      const end = textarea.selectionEnd;
      const val = textarea.value;

      if (event.shiftKey) {
        const lineStart = val.lastIndexOf('\n', start - 1) + 1;
        const spaces = val.slice(lineStart, lineStart + 2);
        if (spaces === '  ') {
          const newVal = val.slice(0, lineStart) + val.slice(lineStart + 2);
          textarea.value = newVal;
          textarea.selectionStart = textarea.selectionEnd = start - 2;
          this.valueChange.emit(newVal);
        }
      } else {
        const newVal = val.slice(0, start) + '  ' + val.slice(end);
        textarea.value = newVal;
        textarea.selectionStart = textarea.selectionEnd = start + 2;
        this.valueChange.emit(newVal);
      }
    }
  }

  @HostListener('document:keydown.escape')
  onEscape(): void {
    this.close.emit();
  }

  onBackdropClick(event: MouseEvent): void {
    if ((event.target as HTMLElement).classList.contains('dialog-backdrop')) {
      this.close.emit();
    }
  }
}
