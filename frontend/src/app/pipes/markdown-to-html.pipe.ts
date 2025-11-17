import { Pipe, PipeTransform } from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';

@Pipe({
  name: 'markdownToHtml',
  standalone: true
})
export class MarkdownToHtmlPipe implements PipeTransform {
  constructor(private sanitizer: DomSanitizer) {}

  transform(value: string | null): SafeHtml {
    if (!value) {
      return '';
    }

    // Simple markdown to HTML converter
    // Headers
    let html = value
      .replace(/^### (.*$)/gm, '<h3>$1</h3>')
      .replace(/^## (.*$)/gm, '<h2>$1</h2>')
      .replace(/^# (.*$)/gm, '<h1>$1</h1>');

    // Bold
    html = html.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');
    
    // Italic
    html = html.replace(/\*(.*?)\*/g, '<em>$1</em>');
    
    // Lists - process before paragraphs and line breaks
    html = html.replace(/^\- (.*$)/gm, '<li>$1</li>');
    // Support '*' as list bullet in addition to '-'
    html = html.replace(/^\* (.*$)/gm, '<li>$1</li>');
    // Wrap consecutive list items in ul tags and remove line breaks between them
    html = html.replace(/(<li>.*?<\/li>(\n<li>.*?<\/li>)*)/g, (match) => {
      return '<ul>' + match.replace(/\n/g, '') + '</ul>';
    });
    
    // Paragraphs
    html = html.replace(/\n\n/g, '</p><p>');
    html = '<p>' + html + '</p>';
    
    // Line breaks (avoid adding them inside existing tags)
    html = html.replace(/\n/g, '<br>');

    return this.sanitizer.bypassSecurityTrustHtml(html);
  }
}
