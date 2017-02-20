package database;

public class IDAndID {
	public Integer WORD1;
	public Integer WORD2;

	public IDAndID(final Integer id1, final Integer id2) {
		WORD1 = id1;
		WORD2 = id2;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof IDAndID))
			return false;
		final IDAndID other = (IDAndID) obj;
		if (WORD1 == null) {
			if (other.WORD1 != null)
				return false;
		} else if (!WORD1.equals(other.WORD1))
			return false;
		if (WORD2 == null) {
			if (other.WORD2 != null)
				return false;
		} else if (!WORD2.equals(other.WORD2))
			return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((WORD1 == null) ? 0 : WORD1.hashCode());
		result = (prime * result) + ((WORD2 == null) ? 0 : WORD2.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "IDAndID [id1=" + WORD1 + ", id2=" + WORD2 + "]";
	}

}
