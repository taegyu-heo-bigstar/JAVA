import java.util.StringTokenizer;
import java.util.HashMap;

// class wordcnt{
// 	String word;
// 	int cnt;
// 	wordcnt(String word) { this.word = word; this.cnt = 1;} // 최초 생성시 무조건 1개.
// 	void add() { cnt++; } // 같은 워드가 등장할때마다 1씩 증가
// 	void show() { System.out.println( word + ":" + cnt); }
// 	static int find(wordcnt[] wc, int wcnt, String tok) 
// 	{ 
// 		for(int i=0; i<wcnt; i++)
// 		{
// 			if (wc[i].word.equals(tok)) // i번째 단어가 tok과 같은가?
// 			{
// 				return i;
// 			}
// 		}
// 		return -1; //wc안에 tok이 없는경우
// 	}
// 	//wc 배열속에 tok이 존재하는지 확인해서 존재하면 인덱스르 반환 없으면 -1 반환
// }

public class Quiz 
{

	public static void main(String[] args) {
		// wordcnt[] wc = new wordcnt[100];
		HashMap<String, Integer> map = new HashMap<>();
		int wcnt = 0;
		String str = "c++ c++ c++ py py ios c#";
		//알고리즘 구현
		StringTokenizer stok = new StringTokenizer(str);
		while(stok.hasMoreTokens())
		{
			String tok = stok.nextToken();
			map.put(tok, map.getOrDefault(tok, 0) + 1);	
			// int idx = wordcnt.find(wc, wcnt, tok);
			// if(idx == -1) //칠판에 토큰이 없음
			// {
			// 	// 새로운단어를 추가
			// }
			// else //칠판에 토큰이 존재
			// {
			// 	// wc[idx]에 단어수를 증가.
			// }
		}
		// wc 배열의 단어와 갯수를 출력
		for(String key : map.keySet())
		{
			System.out.println(key + ":" + map.get(key));
		}
}

