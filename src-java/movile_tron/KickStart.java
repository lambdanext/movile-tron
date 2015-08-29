package movile_tron;

import clojure.lang.IFn;
import clojure.java.api.Clojure;

public final class KickStart {
	public static void main(String[] args) {
		final IFn require = Clojure.var("clojure.core/require");
		final IFn symbol  = Clojure.var("clojure.core/symbol");

		require.invoke(symbol.invoke("movile-tron.aleph"));
		
		Clojure.var("movile-tron.aleph/start-server!").invoke();
	}
}
