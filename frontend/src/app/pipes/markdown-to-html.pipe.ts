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

    let html = value.replace(/\r\n/g, '\n');

    // 1. Bold and Italic
    html = html.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');
    // Match italic, ensuring we don't match list items (* item).
    html = html.replace(/\*([^\s\*][^\*]*?[^\s\*]|[^\s\*])\*/g, '<em>$1</em>');

    // 2. Headers
    html = html.replace(/^### (.*$)/gm, '<h3>$1</h3>');
    html = html.replace(/^## (.*$)/gm, '<h2>$1</h2>');
    html = html.replace(/^# (.*$)/gm, '<h1>$1</h1>');

    // 3. Lists
    // Unordered lists
    html = html.replace(/^[\-|\*]\s+(.*)$/gm, '<li>$1</li>');
    html = html.replace(/(<li>.*?<\/li>(?:\s*<li>.*?<\/li>)*)/g, match => {
      return `<ul>${match.replace(/\n/g, '')}</ul>`;
    });

    // Ordered lists
    html = html.replace(/^\d+\.\s+(.*)$/gm, '<li class="ordered">$1</li>');
    html = html.replace(/(<li class="ordered">.*?<\/li>(?:\s*<li class="ordered">.*?<\/li>)*)/g, match => {
      return `<ol>${match.replace(/\n/g, '')}</ol>`;
    });
    html = html.replace(/<li class="ordered">/g, '<li>');

    // 4. Formatting Blocks
    // Pad block-level elements with double newlines so they can be isolated
    html = html.replace(/(<(?:ul|ol|h[1-6])>)/g, '\n\n$1');
    html = html.replace(/(<\/(?:ul|ol|h[1-6])>)/g, '$1\n\n');

    // Collapse 3 or more newlines into 2
    html = html.replace(/\n{3,}/g, '\n\n');

    // Split into logical blocks
    const blocks = html.split(/\n\n/);
    const processedBlocks = blocks.map(block => {
      const trimmed = block.trim();
      if (!trimmed) return '';

      // If it's already a block-level HTML element, leave it as is
      if (/^<(ul|ol|h[1-6])>/.test(trimmed)) {
        return trimmed;
      }

      // Otherwise, it's a paragraph. Wrap in <p> and convert inner \n to <br>
      return `<p>${trimmed.replace(/\n/g, '<br>')}</p>`;
    });

    html = processedBlocks.filter(b => b.length > 0).join('\n');

    return this.sanitizer.bypassSecurityTrustHtml(html);
  }
}
