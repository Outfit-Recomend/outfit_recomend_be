#!/usr/bin/env python3
"""
API 응답을 HTML 파일로 저장하는 스크립트
"""
import json
import sys
import html

def create_html_from_response(json_data):
    """JSON 응답 데이터로부터 HTML 파일 생성"""
    
    outfit_image_url = json_data.get('outfitImageUrl', '')
    description = json_data.get('description', '')
    prompt = json_data.get('prompt', '')
    search_query = json_data.get('searchQuery', '')
    products = json_data.get('products', [])
    
    # HTML 생성
    html_content = f"""<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>생성된 코디 이미지</title>
    <style>
        body {{
            font-family: Arial, sans-serif;
            padding: 20px;
            background-color: #f5f5f5;
        }}
        .container {{
            max-width: 1200px;
            margin: 0 auto;
            background: white;
            padding: 20px;
            border-radius: 8px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }}
        h1 {{
            color: #333;
        }}
        .info {{
            background: #f9f9f9;
            padding: 15px;
            border-radius: 4px;
            margin: 20px 0;
        }}
        .info p {{
            margin: 5px 0;
        }}
        img {{
            max-width: 100%;
            height: auto;
            border: 1px solid #ddd;
            border-radius: 4px;
            margin: 20px 0;
        }}
        .products {{
            margin-top: 30px;
        }}
        .products h2 {{
            color: #333;
            border-bottom: 2px solid #333;
            padding-bottom: 10px;
        }}
        .product-grid {{
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
            gap: 20px;
            margin-top: 20px;
        }}
        .product-item {{
            border: 1px solid #ddd;
            border-radius: 4px;
            padding: 15px;
            background: #fff;
            transition: box-shadow 0.3s;
        }}
        .product-item:hover {{
            box-shadow: 0 4px 8px rgba(0,0,0,0.1);
        }}
        .product-item img {{
            width: 100%;
            height: 200px;
            object-fit: cover;
            border-radius: 4px;
            margin-bottom: 10px;
        }}
        .product-item h3 {{
            margin: 0 0 10px 0;
            font-size: 14px;
            color: #333;
            height: 40px;
            overflow: hidden;
        }}
        .product-item p {{
            margin: 5px 0;
            font-size: 12px;
            color: #666;
        }}
        .product-item a {{
            display: inline-block;
            margin-top: 10px;
            padding: 8px 15px;
            background: #007bff;
            color: white;
            text-decoration: none;
            border-radius: 4px;
            font-size: 12px;
        }}
        .product-item a:hover {{
            background: #0056b3;
        }}
        .no-products {{
            text-align: center;
            padding: 40px;
            color: #999;
        }}
    </style>
</head>
<body>
    <div class="container">
        <h1>생성된 코디 이미지</h1>
        <div class="info">
            <p><strong>설명:</strong> {html.escape(description)}</p>
            <p><strong>검색 쿼리:</strong> {html.escape(search_query)}</p>
            <p><strong>검색된 상품 수:</strong> {len(products)}개</p>
        </div>
        {f'<img src="{outfit_image_url}" alt="코디 이미지" />' if outfit_image_url else '<p>이미지가 없습니다.</p>'}
        <div class="products">
            <h2>추천 상품 ({len(products)}개)</h2>
"""
    
    if products:
        html_content += '            <div class="product-grid">\n'
        for product in products:
            title = html.escape(product.get('title', '제목 없음'))
            image_url = html.escape(product.get('imageUrl', ''))
            link = html.escape(product.get('link', '#'))
            snippet = html.escape(product.get('snippet', ''))
            
            html_content += f"""            <div class="product-item">
                {f'<img src="{image_url}" alt="{title}" onerror="this.style.display=\'none\'" />' if image_url else ''}
                <h3>{title}</h3>
                {f'<p>{snippet[:100]}...</p>' if snippet else ''}
                {f'<a href="{link}" target="_blank">상품 보기</a>' if link != '#' else ''}
            </div>
"""
        html_content += '            </div>\n'
    else:
        html_content += '            <div class="no-products"><p>상품이 없습니다.</p></div>\n'
    
    html_content += """        </div>
    </div>
</body>
</html>"""
    
    return html_content

def main():
    # JSON 데이터 읽기 (stdin 또는 파일)
    if len(sys.argv) > 1:
        # 파일에서 읽기
        with open(sys.argv[1], 'r', encoding='utf-8') as f:
            json_data = json.load(f)
    else:
        # stdin에서 읽기
        json_data = json.load(sys.stdin)
    
    # HTML 생성
    html_content = create_html_from_response(json_data)
    
    # HTML 파일로 저장
    output_file = 'generated-outfit.html'
    with open(output_file, 'w', encoding='utf-8') as f:
        f.write(html_content)
    
    print(f"✅ HTML 파일이 생성되었습니다: {output_file}")
    print(f"   브라우저에서 열기: open {output_file}")

if __name__ == '__main__':
    main()

