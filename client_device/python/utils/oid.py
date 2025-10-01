from enum import Enum
from typing import List

class OID(Enum):
    CPU_TOTAL_LOAD = "1.4"
    RAM_FREE = "2.1"

    @classmethod
    def values(cls) -> List[str]:
        return [member.value for member in cls]