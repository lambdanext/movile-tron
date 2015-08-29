package movile_tron;

import java.util.Map;
import java.util.List;

import clojure.lang.AFn;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.java.api.Clojure;

public class SmartButt extends AFn {
	final static IFn symbol  = Clojure.var("clojure.core/symbol");
	final static IFn keyword = Clojure.var("clojure.core/keyword");
	final static IFn require = Clojure.var("clojure.core/require");
	final static IFn hmap    = Clojure.var("clojure.core/hash-map");
	
	static {
		require.invoke(symbol.invoke("movile-tron.smartass"));
	}
	
	final static IFn left  = Clojure.var("movile-tron.smartass/left");
	final static IFn right = Clojure.var("movile-tron.smartass/right");
	final static IFn up    = Clojure.var("movile-tron.smartass/up");
	final static IFn down  = Clojure.var("movile-tron.smartass/down");
	final static Keyword colonPos = (Keyword)keyword.invoke("pos");
	
	public long pathLength(IFn direction, List<Long> curPosition,
			Map<List<Long>, Keyword> arena) {
		long i;
		List<Long> pos = curPosition;
		
		for (i = 0; true; i++) {
			pos = (List<Long>)direction.invoke(pos);
			if (arena.containsKey(pos)) {
				break;
			}
		}
		
		return i;
	}
	
	public Map<Keyword, List<Long>> strategy(Map<Keyword, List<Long>> state,
			Map<List<Long>, Keyword> arena) {
		IFn[] directions  = { left, right, up, down };
		List<Long> curPos = state.get(colonPos);
		
		long l = 0;
		IFn best = left;
		for (IFn direction : directions) {
			long candidate = pathLength(direction, curPos, arena); 
			if (l < candidate) {
				l = candidate;
				best = direction;
			}
		}
		
		return (Map<Keyword, List<Long>>)hmap.invoke(colonPos,
				best.invoke(curPos));
	}
	
	@Override
	public Object invoke(Object state, Object arena) {
		return strategy((Map<Keyword,List<Long>>)state,
				(Map<List<Long>,Keyword>)arena);
	}
}
